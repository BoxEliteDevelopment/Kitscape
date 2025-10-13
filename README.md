# KitScape

[![Build Status](https://github.com/BoxEliteDevelopment/KitScape/actions/workflows/build.yml/badge.svg)](https://github.com/BoxEliteDevelopment/KitScape/actions/workflows/build.yml)
[![Release](https://github.com/BoxEliteDevelopment/KitScape/actions/workflows/release.yml/badge.svg)](https://github.com/BoxEliteDevelopment/KitScape/actions/workflows/release.yml)

A powerful and intuitive kit management plugin for Minecraft Paper servers. Create, manage, and distribute custom item kits to your players with an easy-to-use GUI and comprehensive admin tools.

## Features

✨ **Intuitive GUI** - Beautiful graphical interface for browsing and claiming kits  
⏰ **Cooldown System** - Flexible cooldown options (seconds, minutes, hours, days, weeks, months, years, or one-time)  
🔒 **Permission-Based Access** - Control who can access specific kits  
🎨 **Fully Customizable** - Customize messages, GUI appearance, and kit properties  
👁️ **Kit Preview** - Players can preview kit contents before claiming  
💾 **Session Management** - Create kits directly from your inventory  
🎯 **Admin Tools** - Comprehensive commands for kit management  
🔄 **Hot Reload** - Reload configuration without restarting the server  

## Requirements

- **Minecraft Version:** 1.21+
- **Server Software:** Paper/Purpur (or Paper-based forks)
- **Java Version:** 21+

## Installation

1. Download the latest `KitScape-X.X-shaded.jar` from the [Releases](https://github.com/BoxEliteDevelopment/KitScape/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin by editing `plugins/KitScape/config.yml`

## Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kits` | Open the kits GUI | `kitscape.use` (default) |
| `/kitscape help` | Show help menu | `kitscape.use` (default) |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kitscape create <name>` | Start a kit creation session | `kitscape.admin` |
| `/kitscape confirm` | Save your current inventory as a kit | `kitscape.admin` |
| `/kitscape remove <name>` | Delete a kit | `kitscape.admin` |
| `/kitscape reload` | Reload plugin configuration | `kitscape.admin` |
| `/kitscape list [kit]` | List all kits or view kit details | `kitscape.admin` |
| `/kitscape give <kit> <player>` | Give a kit to a player | `kitscape.admin` |
| `/kitscape edit <kit> slot <n>` | Change kit's GUI slot position | `kitscape.admin` |
| `/kitscape edit <kit> displayname <text>` | Set kit's display name | `kitscape.admin` |
| `/kitscape edit <kit> material <material>` | Change kit's icon material | `kitscape.admin` |
| `/kitscape edit <kit> lore add\|remove\|set ...` | Edit kit's lore | `kitscape.admin` |
| `/kitscape edit <kit> cooldown <time>` | Set kit cooldown | `kitscape.admin` |

### Cooldown Format

Cooldowns can be set using the following formats:
- `1s` - 1 second
- `5m` - 5 minutes
- `2h` - 2 hours
- `1d` - 1 day
- `1w` - 1 week
- `1mth` - 1 month
- `1y` - 1 year
- `onetime` - Can only be claimed once

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `kitscape.use` | Access to basic kit commands and GUI | All players |
| `kitscape.admin` | Access to all admin commands | Operators only |
| `kitscape.kit.<kitname>` | Permission to access a specific kit | Set per kit |

## Configuration

The plugin generates a `config.yml` file with customizable options:

```yaml
messages:
  claim: "&aYou claimed &f%kit%&a!"
  preview: "&ePreviewing &f%kit%&e..."
  cooldown: "&cPlease wait &f%time%&c before claiming %kit% again."
  actionbar_claim: "&aYou claimed &f%kit%&a!"
  title_claim:
      title: "&6Kit Claimed!"
      subtitle: "&f%kit% Claimed"

lore:
  claimHint: "&7Left-click to claim"
  previewHint: "&7Right-click to preview"

gui:
  title: "&4Kitscape"
  cooldownLine: "&cCooldown: &f%time%"
  locked:
    show: true
    item:
      material: "GRAY_STAINED_GLASS_PANE"
      name: "&c&lLocked"
      lore:
        - "&7You need: &f%perm%"

branding:
  logo_text: "K I T S C A P E"
  logo_hex: "#FFD700"
```

## Creating a Kit

1. Fill your inventory with the items you want in the kit
2. Run `/kitscape create <kitname>`
3. Run `/kitscape confirm` to save the kit
4. Customize the kit using `/kitscape edit` commands:
   - Set the display name
   - Choose an icon material
   - Add lore descriptions
   - Set cooldown time
   - Assign a GUI slot

## Usage Example

```bash
# Create a starter kit
/kitscape create starter
/kitscape confirm

# Customize the kit
/kitscape edit starter displayname &6Starter Kit
/kitscape edit starter material CHEST
/kitscape edit starter lore add &7Basic items for new players
/kitscape edit starter cooldown onetime
/kitscape edit starter slot 0

# Give the kit to a player
/kitscape give starter PlayerName
```

## Building from Source

This project uses Maven for dependency management and building.

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/BoxEliteDevelopment/KitScape.git
cd KitScape

# Build with Maven
mvn clean package

# The compiled JAR will be in target/KitScape-X.X-shaded.jar
```

### Automated Builds

This project uses GitHub Actions for continuous integration:
- **Build Workflow**: Automatically builds the plugin on every push to master
- **Release Workflow**: Creates releases with JAR files when tags are pushed

## Support

If you encounter any issues or have suggestions:
- Open an issue on [GitHub Issues](https://github.com/BoxEliteDevelopment/KitScape/issues)
- Check existing issues for solutions


