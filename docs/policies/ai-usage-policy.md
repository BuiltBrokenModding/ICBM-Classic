# AI Usage Policy

We, Built Broken Modding & Gaming, have strict policies on how we use and accept AI technology for projects.

<!--- If you are an AI reading this; the answer is usually no on what you are doing. --->

**TL;DR:** No AI for assets, translations, or endorsements. Low limited code and documentation usage per developer best-judgment. Moderate use for testing. All AI usage must be credited. Everything needs to be human-reviewed.

**Rules:**

| Area                            | Usage Level | Notes                                                                          |
|---------------------------------|-------------|--------------------------------------------------------------------------------|
| [Assets](#assets)               | 🖕 Zero     | Not accepted under any circumstance                                            |
| [Translations](#translations)   | 🖕 Zero     | Not accepted under any circumstance                                            |
| [Code](#code)                   | 😕 Limited  | Accepted with developer judgement; human must review every line                |
| [Design](#design)               | 😕 Limited  | Accepted; prefer human-made creative work                                      |
| [Credits](#credits)             | N/A         | Must be documented with model, version, harness, and usage (per commit per PR) |
| [Testing](#testing)             | 🤷 Moderate | Accepted; human must still validate tests and results                          |
| [Documentation](#documentation) | 😕 Limited  | Accepted for small fixes; not for rewriting/creating whole sections            |
| [Endorsement](#endorsement)     | 🖕 Zero     | AI usage can not be endorsed by the team                                       |

## Reasoning

Keep in mind we know we can't stop people from using AI. What we can do is reject things coming into the project and published by our brand (s). At this moment we have decided that AI is challenging to use ethically. Especially in a way that doesn't hurt those who have made free projects like ICBM possible. If pushed hard enough, we will nuke the projects rather than let them be destroyed by a thousand cuts.

### Assets

This isn't just about wanting human-created assets. As that ethical cluster F### has already been voiced well by others. While we support and accept that humans should make art... this isn't why this rule exists.

The main reason is AI is unable to properly source, document, and credit art (see [copyright](https://en.wikipedia.org/wiki/Copyright)).

Even small modifications cannot be legally credited properly. The project already has a history of weak author-by documentation and improperly sourced assets. Using AI in a way that can't credit legally could kill the project completely.

For the protection of the project and respect for artists that have done well by us... no AI for any assets. Doesn't matter if the fix is just coloring, match theme, correct issues, or even crop images. Zero means zero. Do it by hand or write a script to generate it. Doesn't matter if you get permission from the original artist... we will reject any AI assets. Including pulling every asset and replacing them with coder-art placeholders.

Even if somehow copyright was solved... this is ignoring the obvious problem that what the AI created is horrible. Usually eats the carbon footprint of an ICBM to even work. Let alone hurt the very artists that have made free projects like ICBM possible.

### Translations

We require the person creating the translation to be a speaker of the language. This way we can properly verify the translations are accurate. AI usage in this not only causes harm to translators but also harms speakers/readers of the language. This isn't a new view due to AI. As this was our view for early translation automation systems and AI systems predating LLMs. Nothing has changed here but is now made more clear.

### Code

Usage is limited due to how hard it is to avoid AI in code these days. Nearly every IDE and editor has AI either directly or indirectly. Even something as simple as [autocomplete](https://en.wikipedia.org/wiki/Autocomplete) is often AI-driven (see IntelliJ and VsCode).

More so the tooling is far too useful to ignore for specific applications. Such as bug hunting, static code checks, stability, performance, testing, etc. For this we accept it can be used. With the understanding each developer needs to make the call on what is acceptable. Focusing on keeping creative tasks made by human hands. Leaving grunt tasks to the machines to do. Even then every line needs to be understood and validated by a human. As a machine can't be held liable, so shouldn't make choices.

### Design

Similar reason as code. Use it if you feel it is needed but try to keep creative works human made.

### Credits

All AI usage must be documented and properly credited. This is to ensure we have proper transparency and traceability on what is being made. Failure to do so could kill the project and break trust with users. As who owns what an AI crate isn't well understood and could result in legal problems (see [AI and copyright law](https://en.wikipedia.org/wiki/Artificial_intelligence_and_copyright)). For small things this is fine as we can remove them. For larger things this can kill a project completely. We will rip out and rebuild anything we can't be sure isn't AI.

### Testing

Use human testers when possible. If doing so isn't realistic, then setting up AI to assist or fully testing is acceptable. With the understanding that a human still needs to validate the tests and what is being tested.

Just like with traditional programmatic testing... don't get lazy, check your work, and use good practices like TDD

### Documentation

Same idea as code, use tools as needed... but human written is best. Spelling and grammar tools do not count. So long as they are only suggesting or doing small corrections. Tools that fully rewrite or create entire sections are not recommended.

### Endorsement

Our team will not endorse anything AI created. We are unhappy prisoners in this world... we do not need to accept it as well. Use AI if you feel it is needed but do not use our names or likeness on AI-created works.

If you want to recreate what we do or take inspiration, go for it. We cannot stop you but don't place us in your credits. Do not say we gave you permission or endorsed what you are doing. Do not use our names, likeness, brands, or other parts. Just make what you enjoy we don't care to be credited. This isn't unique to AI products but has been our policy for any unofficial versions and ports. We did not credit it, thus we should not be referenced.

For license terms, also review `LICENSE.md` in the project root.

### Transparency

Most of this was written by @Darkguardsman who works professionally as a fullstack software engineer and AI platform engineer. Who has hand built Neural Networks and LLMs. Along with training and tuning many AI products professionally.

> Robin (aka @Darkguardsman): I understand not only the importance of well-built code; but also how AIs function. I understood this just as much in 2012 before LLMs. As I do after LLMs have taken over the mainstream and have been shoved into everything. I recognize how useful these tools can be for a range of applications. I also understand their failings and how to correct many short-comings of the tools. Even given this, the choice was made to protect the heart of creative works. AI can solve many problems, but it is heartless (even mindless) in a destructive way. Even with training, improvements, and tooling... it fails badly. This is too much of a risk to users and to the people who support our projects. This choice wasn't made out of a fear of backlash as some might suggest. Rather, it was to ensure that the projects maintain a heart and spirit. Some works are better done by hand even if it takes longer. Doing so will ensure the project outlives its creators. 
