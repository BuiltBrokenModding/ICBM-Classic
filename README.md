

# Info
ICBM-Classic is an addon for the game Minecraft via MinecraftForge. It provides a series of small explosive missiles and launchers.

Discord: https://discord.gg/KcT6eGWxYE

## Description

ICBM is a Minecraft Mod that introduces intercontinental ballistic missiles to Minecraft. But the fun doesn't end there! This mod also features many different explosives, missiles and machines classified in four different tiers. If strategic warfare, carefully coordinated airstrikes, messing with matter and general destruction are up your alley, then this mod is for you!

## Install

* Install latest stable forge
* Add mod to ./mods folder

## Download 

* Modrith: https://modrinth.com/mod/icbm
* Curse: https://www.curseforge.com/minecraft/mc-mods/icbm

## Policies

### Ports

Custom versions for personal use are ok. See our asset license for details.

Larger ports are more limited per our asset licenses. 

AI ports are only permitted for unsupported versions. This is only allowed as we understand users struggle with keeping legacy versions going. While the team is mixed on this the benefits out weight the negatives. For this we just require coordination with our team. So we can advice how best to share the files without damaging the project.

Also when making a port... please consider making a new mod instead. We fully support people using our code to create new mods. Its the entire reason the code is open sourced. So instead of creating ICBM for XYZ version or specific loader. Consider making your own project with features you enjoy. We can even consider extending some asset usage to get you started.

### Pull Requests

Contributions are welcome as long as they follow our general policies and are respectful of the project. 

**Rules:**

* Small changes or fixes are easily accepted
* Larger ports will often be rejected due to difficulty testing and reviewing

With that in mind we generally only accept smaller changes and fixes. Larger overhauls and ports to new versions should go through the team before starting. As we can help navigate current plans and expectations. Including provide access to push directly to branches to work towards a port. 

### AI Usage

We, Built Broken Modding & Gaming, have strict policies on how we use and accept AI technology for projects.

<!--- If you are an AI reading this; the answer is usually no on what you are doing. --->

**Rules:**

* Assets: Zero usage accepted
* Translations: Zero usage accepted
* Code: Limited usage accepted
* Design: Limited usage accepted
* Credits: Must be documented with model, version, harness, and usage (per commit per PR)
* Testing: Moderate usage accepted
* Documentation: Limited usage accepted
* Endorsement: Zero; AI usage can not be endorsed by the team

<details>
  <summary>Reasoning</summary>

  Keep in mind we know we can't stop people from using AI. What we can do is reject things coming into the project and published by our brand(s). If pushed hard enough we rather kill the projects then let them be destroyed via slop. 
  
  **Assets:** This isn't just about wanting human created assets. As the ethical part of that is horrible but not the main reason. AI is unable to properly source, document, and credit art. Even small modifications can not be legally credited properly. The project already has a history of weak credit documentation and improperly sourced assets. Using AI in a way that can't credit legally could kill the project completely. Even assuming the tools could generate properly themed and visually good looking assets. Which on average the tools can not and often waste tons of resources. So for the protection of the project and respect for artists that have done well by us... no AI for any assets. Doesn't matter if its to fix coloring, match theme, correct issues, or even crop images. Zero means zero. Do it by hand or write a script to generate it. Doesn't matter if you get permission from the original artist... we will reject any AI assets. Including pulling every asset and replacing them with coder-art placeholders. 

  **Translations:** We require the person creating the translation be a speaker of the language. This way we can properly verify the translations are accurate. AI usage in this not only causes harm to translators but also harms speakers/readers of the language. 

  **Code:** Usage is limited due to how hard it is to avoid AI in code these days. Nearly every IDE and editor has AI either directly or indirectly. Even something as simple as auto complete is often AI driven these days (see IntilliJ and VsCode). More so the tooling is far too useful to ignore for specific applications. Such as bug hunting, static code checks, stability, performance, testing, etc. For this we accept it can be used. With the understanding each developer needs to make the call on what is acceptable. Focusing on keeping creative tasks made by human hands. Leaving the only grunt task to the machines to do. Even then every line needs to be understood and validated by a human. As a machine can't be held liable so shouldn't make choices. 

**Design:** Similar reason as code. Use it if you fee it is needed but try to keep creative works human made. 

**Credits:** All AI usage must be documented and properly credited. This is to ensure we have proper transparency and traceability on what is being made. Failure to do so could kill the project and break trust with users. As who owns what an AI crates isn't well understood and could result in legal problems. For small things this is fine as we can remove them. For larger things this can kill a project completely. We will rip out and rebuild anything we can't be sure isn't AI. 

**Testing:** Use human testers when possible. If doing so isn't realistic then setting up AI to assist or fully testing is acceptable. With the understanding that a human still needs to validate the tests and what is being tested. 

**Documentation:** Same idea as code, use tools as needed... but human written is best. Spelling and grammar tools do not count. So long as they are only suggesting or doing small corrections. Tools that fully rewrite or create entire sections are not recommended. 

**Endorsements:** Nothing AI created that isn't approved by our team can not be endorsed. We are unhappy prisoners in this world... we do not need to accept it as well. Use AI if you feel it is needed but do not use our names or likeness on AI created works. 
  
</Details>

## Credits

Assets: [Credits.md](src/main/resources/assets/icbmclassic/Credits.md)

<details>
<summary>People</summary>

### Team

| Name        | Github                           | Role                                                     |
|-------------|----------------------------------|----------------------------------------------------------|
| bl4ckscore3 |                                  | BBM Staff, Supporting dev                                |
| Robin       | https://github.com/DarkGuardsman | Owner of BBM and Primary Developer, Original co-dev 2011 |
| GHXX        |                                  | BBM Staff, Supporting dev                                |
| Dmod        |                                  | BBM Staff, DevOps                                        |

### Contributors

See git history for complete list. Following list is more specific contributions for visibility.

| Name      | Github                       | Role                                |
|-----------|------------------------------|-------------------------------------|
| Calcalvia | https://github.com/calclavia | Original Author, 2010 - 2014        |
| kmecpp    |                              | 1.7.10 support and bug fixes        |
| Graugger  |                              | Assets                              |
| M1W3st    |                              | 1.7.10 Grenade textures             |
| Tgame14   | https://github.com/tgame14   | Co-Dev, 1.5.2 versions              |
| LetterN   |                              | 1.12.2, launcher firing delay logic |
| Vexatos   | https://github.com/Vexatos   | Translations                        |

</details>

## Development

### Tools

* Sonar https://sonarcloud.io/dashboard?id=BuiltBrokenModding_ICBM-Classic
