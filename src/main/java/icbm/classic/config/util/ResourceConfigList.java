package icbm.classic.config.util;

import icbm.classic.ICBMClassic;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prefab for anything that allows blacklist based on {@link ResourceLocation}
 * <p>
 * Life Cycle:
 * - unlock
 * - clear existing
 * - load(external inputs)
 * - batch(registry)
 * - lock
 * <p>
 * Documentation <a href="https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-resource-list">Resource List Config</a>
 */
public abstract class ResourceConfigList<CONFIG extends ResourceConfigList, CONTENT, VALUE> {
    // TODO once ContentBuilder Json system is published for MC replace this with JSON version to support programmatic and more complex entries
    //      entries to consider once JSON is allowed: time/date specific, conditional statements such as IF(MOD) IF(WORLD) IF(MATH) IF(GEO_AREA), block sets/lists

    protected static final Pattern SORTING_REGEX = Pattern.compile("^@sort\\((\\d*),(\\S*)\\)$");
    protected static final Pattern DOMAIN_VALE_REGEX = Pattern.compile("^@domain:([^=]*)(?:=(\\S))?$");

    protected static final Pattern KEY_VALUE_REGEX = Pattern.compile("^([^\\s@]*):([^\\s=]*)(?:=(\\S*))?");

    // Constructor
    @Getter
    private final String name;
    private final String configUrl; //TODO load from a JSON localization metadata file for better dev configuration and traceability of URL usage
    private final Consumer<CONFIG> reloadCallback;

    /** Matchers for handling input entries */
    protected final List<Pair<Pattern, MatcherHandler<CONTENT, VALUE>>> matcherHandlers = new ArrayList<>();

    // Block lists
    protected final Map<ResourceLocation, List<ResourceConfigEntry<CONTENT, VALUE>>> contentMatchers = new HashMap();
    protected final Map<ResourceLocation, List<ResourceConfigEntry<CONTENT, VALUE>>> defaultMatchers = new HashMap();
    protected final List<ResourceConfigEntry<CONTENT, VALUE>> generalMatchers = new ArrayList<>();

    /** Issues with parsing data is stored as source -> entries -> issues as string & is-error  */
    private final Map<String, Map<String, List<Pair<String, Boolean>>>> parsingIssues = new HashMap<>(); //TODO maybe a data structure with sorting?

    // States
    @Getter
    private boolean isLocked = false;

    public ResourceConfigList(final String name, final String configUrl, final Consumer<CONFIG> reloadCallback) {
        this.name = name;
        this.configUrl = configUrl;
        this.reloadCallback = reloadCallback;
        addMatcher(DOMAIN_VALE_REGEX, this::handleDomain);
    }

    protected void addMatcher(Pattern pattern, MatcherHandler<CONTENT, VALUE> handler) {
        this.matcherHandlers.add(Pair.of(pattern, handler));
    }

    public void reload() {
        this.unlock();
        this.reset();
        // Let the parent load defaults and pull in configs
        this.reloadCallback.accept((CONFIG) this);
        this.processData();
        // Sort functions
        sort(generalMatchers);
        contentMatchers.values().forEach(this::sort);
        defaultMatchers.values().forEach(this::sort);
        this.lock();

        outputParsingIssues();
        outputResults();
    }

    protected void outputParsingIssues() {
        //TODO configs to ignore issues
        if(!parsingIssues.isEmpty()) {
            ICBMClassic.logger().info( "{}: parser documentation can be found at {}",  this.getName(), this.configUrl);
            ICBMClassic.logger().error("{}: Parsing errors detected:", this.getName());

            //TODO abc sort source, entry, and issues
            parsingIssues.forEach((source, entries) -> {
                ICBMClassic.logger().error("\t{}", source);
                entries.forEach((entry, issues) -> {
                    ICBMClassic.logger().error("\t\t{}", entry);
                    issues.forEach(p -> {
                        if(p.getValue()) {
                            ICBMClassic.logger().error("\t\t-{}", p.getKey());
                        }
                        else {
                            ICBMClassic.logger().warn("\t\t-{}", p.getKey());
                        }
                    });
                });
            });
        }
    }

    protected void outputResults() {
        //TODO implement debugging output of results. Likely want to make a map of keys to matches showing type and sort order
    }

    protected void sort(List<ResourceConfigEntry<CONTENT, VALUE>> list) {
        // Generate defaults
        int highestUserSort = list.stream().map(ResourceConfigEntry::getOrder).mapToInt(i -> i == null ? 0 : i).max().orElse(0);

        int index = 0;
        for (ResourceConfigEntry<CONTENT, VALUE> func : list) {
            if (func.getOrder() == null) {
                index++;
                func.setOrder(highestUserSort + index);
            }
        }

        // Do sort
        list.sort(this::compare);
    }

    protected int compare(ResourceConfigEntry<CONTENT, VALUE> a, ResourceConfigEntry<CONTENT, VALUE> b) {
        int sortA = a.getOrder();
        int sortB = b.getOrder();
        return sortB - sortA;
    }

    protected void processData() {

    }

    private void unlock() {
        this.isLocked = false;
    }

    private void reset() {
        contentMatchers.clear();
        generalMatchers.clear();
        parsingIssues.clear();
    }

    private void lock() {
        this.isLocked = true;
    }

    public void setDefault(ResourceLocation key, VALUE value, int order) {
        defaultMatchers
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(new ResourceConfigEntry<>("default", order, getSimpleValue(key, value)));
    }

    public VALUE getValue(CONTENT state) {
        if (state == null) {
            return null;
        }
        final ResourceLocation key = getContentKey(state);

        final VALUE contentValue = getValue(state, contentMatchers.get(key));
        if (contentValue != null) return contentValue;

        for (Function<CONTENT, VALUE> matcher : generalMatchers) {
            final VALUE result = matcher.apply(state);
            if (result != null) {
                matcherHit(state, matcher);
                return result;
            }
        }

        return getValue(state, this.defaultMatchers.get(key));
    }

    private VALUE getValue(CONTENT state, List<ResourceConfigEntry<CONTENT, VALUE>> matchers) {
        if (matchers != null) {
            for (Function<CONTENT, VALUE> matcher : matchers) {
                final VALUE result = matcher.apply(state);
                if (result != null) {
                    matcherHit(state, matcher);
                    return result;
                }
            }
        }
        return null;
    }

    protected void matcherHit(CONTENT content, Function<CONTENT, VALUE> matcher) {

    }

    protected abstract ResourceLocation getContentKey(CONTENT content);

    /**
     * Loads entries converting them into key:value functions
     *
     * @param entries to process
     */
    public void load(String source, String... entries) {
        //TODO rethink loading to abstract away from caller and better track source(s)
        if (checkLock("entries", () -> String.join(", ", entries))) {
            //TODO trim string to not dump 1000s of lines into logs
            return;
        }

        for (String str : entries) {
            handleEntry(source, str, null);
        }
    }

    private boolean checkLock(String type, Supplier<String> entry) {
        if (this.isLocked) {
            ICBMClassic.logger().error("{}: list is locked. Unable to add '{}' entry '{}'", name, type, entry.get(), new IllegalArgumentException());
            return true;
        }
        return false;
    }

    /**
     * Handles the entry and parses it for conversion to a matcher
     *
     * @param source of the entry, usually path to a config
     * @param entryRaw to parse
     * @param index of the entry
     * @return true if the entry was accepted, even if it failed to parse
     */
    protected final boolean handleEntry(String source, String entryRaw, Integer index) {

        final String entry = entryRaw.replaceAll("\\s", ""); //Kill spaces off -> "M I N E C R A F T" turns into "MINECRAFT"

        if(entry.length() != entryRaw.length()) {
            warn(source, entry, "Entry contains extra whitespace characters '" + entryRaw + "'");
        }

        // TODO allow permutation arguments
        final Matcher sortMatcher = SORTING_REGEX.matcher(entry);
        if (sortMatcher.matches()) {
            return handleEntry(source, sortMatcher.group(2), Integer.parseInt(sortMatcher.group(1)));
        }

        for(Pair<Pattern, MatcherHandler<CONTENT, VALUE>> handlerEntry: this.matcherHandlers) {
            final Matcher regexMatcher = handlerEntry.getKey().matcher(entry);
            if(regexMatcher.matches()) {
                final ResourceConfigEntry<CONTENT, VALUE> resourceConfigEntry = handlerEntry.getValue().handle(regexMatcher, source, entry, index);
                if (resourceConfigEntry != null) {
                    if (resourceConfigEntry.getKey() == null) {
                        this.generalMatchers.add(resourceConfigEntry);
                    } else {
                        this.contentMatchers
                            .computeIfAbsent(resourceConfigEntry.getKey(), (ResourceLocation k) -> new ArrayList<>())
                            .add(resourceConfigEntry);
                    }
                }
                return true;
            }
        }

        error(source, entry, "Unknown format");
        return false;
    }

    protected ResourceConfigEntry<CONTENT, VALUE> handleDomain(Matcher domainMatcher, String source, String entry, int index) {

        final String domain = domainMatcher.group(1);

        if(!isDomainValid(domain)) {
            error(source, entry, "No matching mod domain found for '" + domain + "'");
            return null;
        }

        final String valueStr = domainMatcher.group(2);
        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }

        final Function<CONTENT, VALUE> matcher = getDomainValue(domain, value);
        if(matcher == null) {
            return null;
        }

        return new ResourceConfigEntry<>("resource_domain", index, matcher);
    }

    protected ResourceConfigEntry<CONTENT, VALUE> handleSimple(Matcher regexMatcher, String source, String entry, Integer index) {
        final String domain = regexMatcher.group(1);
        final String resource = regexMatcher.group(2);
        final ResourceLocation key = new ResourceLocation(domain, resource);

        if(!isDomainValid(key.getNamespace())) {
            error(source, entry, "No matching mod domain found for '" + key + "'");
            return null;
        }
        else if(!isValidKey(key)) {
            error(source, entry, "No matching content found for '" + key + "'");
            return null;
        }

        final String valueStr = regexMatcher.group(3);
        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }
        final Function<CONTENT, VALUE> matcher = getSimpleValue(key, value);
        if(matcher == null) {
            return null;
        }
        return new ResourceConfigEntry<>("resource_simple", index, matcher).setKey(key);
    }

    protected Function<CONTENT, VALUE> getDomainValue(String domain, @Nullable VALUE value) {
        return (content) -> domain.equalsIgnoreCase(getContentKey(content).getNamespace()) ? value : null;
    }

    protected Function<CONTENT, VALUE> getSimpleValue(ResourceLocation targetKey, @Nullable VALUE value) {
        // TODO consider caching for faster matching.
        //      As string compare will be slower than object.is(object) for Items and Blocks
        // Contains: ~stone~ ->  dark_stone_deep
        if(targetKey.getPath().startsWith("~") && targetKey.getPath().endsWith("~")) {
            final String checkStr = targetKey.getPath().substring(1, targetKey.getPath().length() - 1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!targetKey.getNamespace().equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return  targetKey.getPath().contains(checkStr) ? value : null;
            };
        }
        // Starts: ~stone -> dark_stone
        else if(targetKey.getPath().startsWith("~")) {
            final String checkStr = targetKey.getPath().substring(1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!targetKey.getNamespace().equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return  targetKey.getPath().endsWith(checkStr) ? value : null;
            };
        }
        // Ends: stone~ -> stone_dark
        else if(targetKey.getPath().endsWith("~")) {
            final String checkStr = targetKey.getPath().substring(0, targetKey.getPath().length() - 1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!targetKey.getNamespace().equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return  targetKey.getPath().startsWith(checkStr) ? value : null;
            };
        }

        // equals: a is a
        return (content) -> targetKey.equals(getContentKey(content)) ? value : null;
    }

    protected abstract VALUE parseValue(String source, String entry, String value);

    /**
     * Checks if the resource key is valid. For content registries
     * this should check contains.
     *
     * @param key to check
     * @return true if valid
     */
    protected boolean isValidKey(ResourceLocation key) {
        return true;
    }

    protected boolean isDomainValid(String domain) {
        //TODO fuzzy domains?
        return "minecraft".equalsIgnoreCase(domain) || ModList.get().isLoaded(domain);
    }

    protected void error(String source, String entry, String error) {
        issue(source, entry, error, true);
    }

    protected void warn(String source, String entry, String error) {
        issue(source, entry, error, false);
    }

    protected void issue(String source, String entry, String error, boolean isError) {
        parsingIssues
            .computeIfAbsent(source, (k) -> new HashMap<>())
            .computeIfAbsent(entry, (k) -> new ArrayList<>())
            .add(Pair.of(error, isError));
    }

    @FunctionalInterface
    protected interface MatcherHandler<CONTENT, VALUE> {
        ResourceConfigEntry<CONTENT, VALUE> handle(Matcher regexMatcher, String source, String entry, Integer index);
    }
}
