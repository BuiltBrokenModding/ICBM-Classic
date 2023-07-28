# Projectile System

Note: This will eventually be moved to a library to better support other mods.

Projectile system views all entities as projectiles. In a way this system just describes the type and usage of each projectile. Such as arrows being fired and snowballs thrown. It also helps integrate with other systems such as explosives.

## Capabilities

ItemStacks are provided with a capability to describe how to spawn and manage a projectile. For entities that have passengers it is recommended to not spawn then in the newEntity call. Instead, wait for post spawn call to add those.