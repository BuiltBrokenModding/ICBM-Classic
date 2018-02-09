package com.builtbroken.mc.core.deps;

/**
 * Used to cache a version number
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/12/2015.
 */
public class Version
{
    public final int major;
    public final int minor;
    public final int revis;
    public final int build;

    public Version(int major, int minor, int revis, int build)
    {
        this.major = major;
        this.minor = minor;
        this.revis = revis;
        this.build = build;
    }

    public Version(String version)
    {
        if (version == null || version.isEmpty())
        {
            throw new IllegalArgumentException("Version can't be created with an empty or null string value");
        }

        int firstDot = version.indexOf(".");
        int secondDot = version.indexOf(".", firstDot + 1);
        int thirdDot = version.indexOf("b", secondDot + 1);

        if (firstDot < 0 || secondDot < 0 || thirdDot < 0)
        {
            throw new IllegalArgumentException("Invalid version string " + version);
        }

        try
        {
            major = Integer.parseInt(version.substring(0, firstDot));
            minor = Integer.parseInt(version.substring(firstDot + 1, secondDot));
            revis = Integer.parseInt(version.substring(secondDot + 1, thirdDot));
            build = Integer.parseInt(version.substring(thirdDot + 1, version.length()));
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to parse version string '" + version + "'", e);
        }
    }

    /**
     * Checks if the version passed in is newer than this one
     *
     * @param v - version
     * @return true if it is newer
     */
    public boolean isNewer(Version v)
    {
        if (v.major < this.major)
            return false;
        if (v.minor < this.minor)
            return false;
        if (v.revis < this.revis)
            return false;
        if (v.build <= this.build)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        if(build == -1)
        {
            return major + "." + minor + "." + revis;
        }
        return major + "." + minor + "." + revis + "b" + build;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Version)
        {
            return ((Version) obj).major == major && ((Version) obj).minor == minor && ((Version) obj).revis == revis && ((Version) obj).build == build;
        }
        return false;
    }
}
