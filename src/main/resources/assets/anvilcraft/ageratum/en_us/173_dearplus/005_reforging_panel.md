---
navigation:
  title: "§5Reforging Panel"
  icon: "anvilcraft_dearplus:reforging_panel"
items:
  - anvilcraft_dearplus:reforging_panel
---

# <ref item="anvilcraft_dearplus:reforging_panel"/>

<color=#B78766> F*CK the POSSIBILITY </color>

<recipe id="anvilcraft_dearplus:reforging_panel"/>

# Quick Start
- **Place** it against the bottom edge of the Celestial Forging Anvil
- Configure **filters** in the GUI
- **Activate** the panel with a redstone signal
- **Wait** for a matching celestial body to appear

# Function
- All **sides** of the panel are inputs. When an input receives a redstone signal, the panel starts **automatic reforging**:
  - When the celestial body in the anvil **satisfies** the filter conditions, the anvil is locked, and a weak power signal of strength 3 is sent to the anvil, above, and below
  - When the celestial body in the anvil **does not satisfy** the filter conditions, the anvil is unlocked and reforging is triggered


- Filter conditions can be configured via the GUI (right-click to open):
  - **Mag. Field**: 6 levels from "None" to "Very Strong"
  - **Rot. Speed**: 5 levels from "Very Slow" to "Very Fast"
  - **Planetary**: None / Biological / Civilization / Any

  Each condition can be independently enabled or disabled; the anvil is only locked when all enabled conditions are satisfied
