package org.smileaf.ava;

import java.io.Serializable;
import java.util.ArrayList;

import org.smileaf.Utils;
import org.smileaf.Color;
import org.smileaf.game.Creature;
import org.smileaf.game.Dialog;
import org.smileaf.game.Map;
import org.smileaf.game.Participant;
import org.smileaf.game.Ability;
import org.smileaf.game.Buff;
import org.smileaf.game.Player;
import org.smileaf.game.ResourceStore;

/**
 *  Where and how the battles take place.
 * @author smileaf (smileaf@me.com)
 */
public class Battle implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  Ability Conditions
	 */
	public static final int INEFFECTIVE = 0;
	/**
	 *  Ability Conditions
	 */
	public static final int DAMAGE		= 1;
	/**
	 *  Ability Conditions
	 */
	public static final int HEAL		= 2;
	/**
	 *  Ability Conditions
	 */
	public static final int MISS   		= (int) Math.pow(2, 2);
	/**
	 *  Ability Conditions
	 */
	public static final int BUFF   		= (int) Math.pow(2, 3);
	/**
	 *  Ability Conditions
	 */
	public static final int DEBUFF 		= (int) Math.pow(2, 4);
	/**
	 *  Ability Conditions
	 */
	public static final int DEBUFFMISS	= (int) Math.pow(2, 5);
	/**
	 *  Ability Conditions
	 */
	public static final int BUFFMISS	= (int) Math.pow(2, 6);
	/**
	 *  Ability Conditions
	 */
	public static final int REFLECT		= (int) Math.pow(2, 7);
	
	/**
	 *  Battle Conditions
	 */
	public static final int WIN  = 0;
	/**
	 *  Battle Conditions
	 */
	public static final int LOSE = 1;
	/**
	 *  Battle Conditions
	 */
	public static final int RAN  = 2;
	/**
	 * Monsters in the Battle
	 */
	private ArrayList<Creature> monsters = new ArrayList<Creature>();
	/**
	 * Player
	 */
	private Player player;

	private double maxTurn = 0;	// amount to accumulator before its the participant's turn

	private double paTurn = 0;	// Player Turn Accumulator
	
	// Used by moves to determine if its the 1st turn or not.
	private int turnCount = 0;
	
	private Dialog dialog;

	public Battle (Dialog dialog, Player player) {
		setDialog(dialog);
		addPlayer(player);
	}
	public Battle (Dialog dialog, Player player, Creature monster) {
		setDialog(dialog);
		addPlayer(player);
		addCreature(monster);
	}
	public Battle (Dialog dialog, Player player, ArrayList<Creature> monsters) {
		setDialog(dialog);
		addPlayer(player);
		addCreatures(monsters);
	}
	/**
	 * Adds creatures to the battle
	 * @param monsters monster to enter the battle.
	 */
	public void addCreatures (ArrayList<Creature> monsters) {
		for (Creature monster : monsters) {
			addCreature(monster);
		}
	}
	/**
	 * Calculates the speeds of all participants
	 */
	public void checkSpeeds() {
		maxTurn = player.speed();
		for (Creature monster : monsters) {
			if (monster.speed() > maxTurn)
				maxTurn = monster.speed();
		}
	}
	/**
	 * Adds a Creature to the battle.
	 * @param monster Monster to add to the battle
	 * @param level Map Level
	 */
	public void addCreature (Creature monster, int level) {
		dialog.setMonster(monster);
		monster.startBattle(level);
		dialog.dialog = "Encountered a ";
		if (monster.type == Creature.CHAMPION) {
			monster.makeBoss(App.game.map.z() * 2 + 2);
			dialog.dialog += new Color("Champion ").fg(Color.MAGENTA);
		} else if (monster.type == Creature.BOSS) {
			monster.makeBoss(App.game.map.z() * 2 + 3);
			dialog.dialog += new Color("Boss ").fg(Color.BLUE);
		}
		this.dialog.nl();
		dialog.println("%M Lvl: " + monster.level()+ "!");
		dialog.println("A:" + monster.attack() + 
						  " D:" + monster.defense() + 
						  " S:" + monster.speed() + 
						  " M:" + monster.mp() + "/" + monster.magic());
		monsters.add(monster);
		checkSpeeds();
		
	}
	/**
	 * Adds a single Monster to the Battle
	 * @param monster Monster to add.
	 */
	public void addCreature (Creature monster) {
		int level = App.game.map.z();
		if (App.game.map.isReturnTrip()) level = (Map.maxZ - App.game.map.z()) + Map.maxZ;
		addCreature(monster, level);
	}
	/**
	 * Sets the Player
	 * @param player Player
	 */
	public void addPlayer (Player player) {
		this.player = player;
		this.player.inBattle = true;
	}
	/**
	 * Sets the dialog
	 * @param dialog Dialog object to handle the output.
	 */
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * Calculate the damage and returns the condition. 
	 * @param p1 Attacking participant
	 * @param p2 Defending participant
	 * @param ability Ability to use
	 * @return Condition
	 */
	public int doAttack(Participant p1, Participant p2, Ability ability) {
		int cond = Battle.INEFFECTIVE;
		float att;
		float def;
		// use attack for physical damage, otherwise its a magic attack.
		if (ability.isPhysical()) att = p1.attack();
		else att = p1.magic();
		att = Math.round(att * ability.getScaling() + ability.getPower());

		// If we're Physically attacking, use the player's Defense,
		// Otherwise use the Player's Magical Defense skill.
		if (ability.isPhysical())
			def = p2.defense();
		else
			def = p2.magicDefense();
		
		int hitChance = Utils.random(0, 100);
		if (hitChance - p2.getbStats().evasion >= ability.getAccuracy(Ability.NORMAL)) {
			// If we're taking damage, we need to calculate and set the damage
			// against our own defense.
			if (ability.hasEffect(Ability.DMGONFAIL)) {
				if (ability.isPhysical())
					def = p1.defense();
				else
					def = p1.magicDefense();
				cond |= Battle.REFLECT;
			}
			cond |= Battle.MISS;	// Missed
		}

		if (ability.hasBuff()) {
			if (Utils.random(0, 100) <= ability.getAccuracy(Ability.BUFF)) {
				ability.buff().reset();
				p1.addBuff(ability.buff());
				cond |= Battle.BUFF;
			} else {
				cond |= Battle.BUFFMISS;
			}
		}
		if (ability.hasDebuff()) {
			if (Utils.random(0, 100) <= ability.getAccuracy(Ability.DEBUFF)) {
				ability.debuff().reset();
				p2.addBuff(ability.debuff());
				cond |= Battle.DEBUFF;
			} else {
				cond |= Battle.DEBUFFMISS;
			}
		}

		if (ability.hasEffect(Ability.DAMAGE)) {
			dialog.damage = doDamage(att, def);
			cond |= Battle.DAMAGE;
		}
		if (ability.hasEffect(Ability.HEAL)) {
			dialog.healing = (int) att;
			cond |= Battle.HEAL;
		}
		if (ability.hasEffect(Ability.ABSORB))
			dialog.healing = dialog.damage;
		return cond;
	}
	/**
	 * Calculate the damage caused by a debuff.
	 * @param p1 Attacking
	 * @param p2 Defending
	 * @param debuff used in the attack
	 * @return damage caused.
	 */
	public static float doDebuffDamage (Participant p1, Participant p2, Buff debuff) {
		if (debuff.isPhysical())
			return doDamage(Math.round(p1.attack() * debuff.getScaling() + debuff.getPower()), p2.defense());
		else
			return doDamage(Math.round(p1.magic() * debuff.getScaling() + debuff.getPower()), p2.magicDefense());
	}
	/**
	 * Calculates the Healing caused by a Buff
	 * @param p1 Current Participant
	 * @param buff Buff to calculate
	 * @return Healing done.
	 */
	public static float doBuffHealing (Participant p1, Buff buff) {
		if (buff.isPhysical())
			return Math.round(p1.attack() * buff.getScaling() + buff.getPower());
		else
			return Math.round(p1.magic() * buff.getScaling() + buff.getPower());
	}
	/**
	 * Calculates the Damage done
	 * @param att Current Participant's Attack
	 * @param def Opponent's Defense
	 * @return Damage caused.
	 */
	public static int doDamage(float att, float def) {
		// We should always at least do 1 damage.
		if (def >= att && def <= att * 2) return 1;
		// Unless their defense is WAY too high.
		if (def > att * 2) return 0;
		return Math.round(att - def);
	}
	
	/**
	 * Calculates all buffs and debuffs
	 * @param p1 Current Participant
	 * @param p2 Opponent
	 * @param startTurn True if we're at the beginning of the Participant's turn.
	 * @return Returns true if the current Participant loses their turn.
	 */
	public boolean doBuffs(Participant p1, Participant p2, boolean startTurn) {
		dialog.damage = 0; // Reset our damage
		dialog.healing = 0;
		boolean skipTurn = false;
		// Buff / debuff duration checks here.
		ArrayList<Buff> buffs;
		// Grab a list of buffs/debuffs that take effect at the start or end of the turn.
		if (startTurn) {
			buffs = ResourceStore.filter(p1.buffs(), c -> !((Buff) c).hasEffect(Ability.ONTURNEND));
		} else {
			buffs = ResourceStore.filter(p1.buffs(), c -> ((Buff) c).hasEffect(Ability.ONTURNEND));
		}
		// We don't want to remove things immediately, so save them for later.
		ArrayList<Buff> toRemove = new ArrayList<Buff>();
		for (Buff buff : buffs) {
			// Check if its a debuff
			if (buff.isDebuff()) {
				// Expire Debuffs 1st before we calculate anything.
				if (buff.expired()) {
					toRemove.add(buff);
					continue;
				} else buff.expire();
				// OR is there a chance for something to happen?
				if (buff.hasEffect(Ability.CHANCETO)) {
					// Calculate chance to lose effect or activate effect.
					int chance = Utils.random(0, 100);
					if (buff.chance() > chance) {
						// Chance to success! lost the turn.
						if (buff.hasEffect(Ability.LOSETURN)) {
							skipTurn = true;
						}
						// Chance to success! now deal damage.
						if (buff.hasEffect(Ability.DAMAGE)) {
							dialog.damage = (int)doDebuffDamage(p2, p1, buff);
							p1.setHP(p1.hp() - dialog.damage);
						}
						dialog.println(buff.dialog().buffPeriod);
						// Remove the debuff. if we are to on success.
						if (buff.removeOnSuccess())
							toRemove.add(buff);
					} else {
						// Chance failed!
						dialog.println(buff.dialog().buffFailed);
						// Remove it if we are to on failure.
						if (buff.removeOnFail())
							toRemove.add(buff);
					}
				// Deal the damage!
				} else if (buff.hasEffect(Ability.DAMAGE)) {
					dialog.damage = (int)doDebuffDamage(p2, p1, buff);
					p1.setHP(p1.hp() - dialog.damage);
					dialog.println(buff.dialog().buffPeriod);
				}
			} else {
				// Calculate if we're expiring
				if (buff.expired()) {
					toRemove.add(buff);
					continue;
				} else buff.expire();
				// If its a heal
				if (buff.hasEffect(Ability.HEAL)) {
					// Is it just a chance to heal?
					if (buff.hasEffect(Ability.CHANCETO)) {
						int chance = Utils.random(0, 100);
						if (buff.chance() < chance) {
							// Chance to heal: Success
							dialog.damage = (int) doBuffHealing(p1, buff);
							p1.setHP(p1.hp() + dialog.damage);
							dialog.println(buff.dialog().buff);
							// Remove it if we are to do so on a success
							if (buff.removeOnSuccess())
								toRemove.add(buff);
						} else {
							// Buff Chance failed
							dialog.println(buff.dialog().buffFailed);
							// Remove it if we are to do so on a failure.
							if (buff.removeOnFail())
								toRemove.add(buff);
						}
					// Guarantee heal!
					} else {
						dialog.damage = (int) doBuffHealing(p1, buff);
						p1.setHP(p1.hp() + dialog.damage);
						dialog.println(buff.dialog().buffPeriod);
					}
				} else {
					// Buff Non-heal
				}
			}
		}
		// Clear all buffs/debuffs.
		// Cannot use removeAll else we can't remove the buffs.
		if (toRemove.size() > 0) {
			for (Buff a : toRemove) {
				dialog.i(a.name() + " expired.");
				p1.removeBuff(a);
			}
		}
		return skipTurn;
	}
	/**
	 * Checks for an effect within inEffect
	 * @param effect Effect to check
	 * @param inEffect Effects the check.
	 * @return True if the bit is set.
	 */
	public static boolean hasCond(int effect, int inEffect) {
		return ((effect & inEffect) == effect);
	}
	/**
	 * Calculates the number of rotations until the Participant's next turn. 
	 * @param player Current Participant
	 * @param turnCounter Participant's turn counter.
	 * @return number of rotations or steps until the Participant gets their next turn.
	 */
	public int stepsToTurn(Participant player, double turnCounter) {
		return (int) (this.maxTurn - turnCounter + Math.round(this.maxTurn / (player.speed() / turnCounter)));
	}
	/**
	 * Displays Information about the Battle during the Player's turn.
	 */
	public void battleDisplay() {
		this.dialog.nl();
		dialog.dialog = "-%Player: HP: " + Color.colorizeValues((int)player.hp(), (int)player.stamina()) +
				" MP: "+ Color.colorizeValues((int)player.mp(), (int)player.magic());
		//if (this.stepsToTurn(this.monster, this.maTurn) > this.stepsToTurn(player, this.paTurn)) dialog.dialog += " (Double attack)";
		dialog.println();
		// Buffs & Debuffs
		if (player.buffs().size() > 0) {
			dialog.dialog = "Buffs & Debuffs:";
			for (Buff a : player.buffs()) {
				if (a.isDebuff())
					dialog.dialog += " D:" + (new Color(a.toString()).fg(Color.RED));
				else
					dialog.dialog += " B:" + (new Color(a.toString()).fg(Color.GREEN));
			}
			dialog.println();
		}
		for (Creature monster : monsters) {
			dialog.dialog = "-%M: HP: " + Color.colorizeValues((int)monster.hp(), (int)monster.stamina()) +
					" MP: "+ Color.colorizeValues((int)monster.mp(), (int)monster.magic());
			//if (this.stepsToTurn(this.monster, this.maTurn) < this.stepsToTurn(player, this.paTurn)) dialog.dialog += " (Double attack)";
			dialog.println();
			// Buffs & Debuffs
			if (monster.buffs().size() > 0) {
				dialog.dialog = "Buffs & Debuffs:";
				for (Buff a : monster.buffs()) {
					if (a.isDebuff())
						dialog.dialog += " D:" + (new Color(a.toString()).fg(Color.RED));
					else
						dialog.dialog += " B:" + (new Color(a.toString()).fg(Color.GREEN));
				}
				dialog.println();
			}
		}
	}
	
	/**
	 * Gets an Ability to use
	 * @param p1 Current Participant
	 * @return null if the player runs, or a monster is out of usable moves, Otherwise Returns the Ability chosen.
	 */
	public Ability getAbility(Participant p1) {
		ArrayList<Ability> ua = p1.useableAbilities();
		Ability ability = null;
		if (p1.isPlayer()) {
			this.battleDisplay();
			Player player = (Player) p1;
			dialog.println("Turn: " + this.turnCount + " (F)ight (D)efend (R)un");
			if (ua.size() > 1) {
				dialog.print("Abilities: ");
				for (int i = 2; i < ua.size(); i++) {
					if (!ua.get(i).name().equals(""))
						dialog.print((i-2) + ": " + ua.get(i).name() + "(MP:" + ua.get(i).calcCost(player.level()) + ") ");
				}
				this.dialog.nl();
			}
			try {
				chooseAbility: while (ability == null || player.isCharging) {
					dialog.print(new Color("What would you like to do? ").fg(Color.CYAN).toString());
					char key = App.terminal.readInput().getCharacter();
					switch (key) {
						case 'f':  ability = player.ability().get(1); break chooseAbility;	// Melee
						case '0': case '1': case '2': case '3': case '4':
						case '5': case '6': case '7': case '8': case '9':
							// ascii 48 is 0, we need to offset the index by 2.
							// So we subtract 46.
							int ic = Character.getNumericValue(key)+2;
							if (player.ability().size() > ic && 
									player.ability().get(ic).calcCost(player.level()) <= player.mp() &&
									!player.ability().get(ic).name().equals("")) {
								ability = player.ability().get(ic);
								if (ability.cooldown() > 0) {
									if (ability.onCooldown()) {
										Dialog.important("Unable to use " + ability.name() + " while on cooldown");
										ability = null;
										break;
									} else {
										ability.used();
									}
									break chooseAbility;
								}
								if (ability.chargeUp() > 0) {
									if (ability.charged()) {
										// OK to use ability!
										player.isCharging = false;
										break chooseAbility;
									} else {
										// Not OK to use ability!
										player.isCharging = true;
										dialog.println(ability.dialog().charging);
										ability = null;
										break;
									}
								}
							} else {
								Dialog.important("Unable to use that!");
							}
							break;
						case 'd': ability = player.ability().get(0); break chooseAbility;
						case 'r': this.dialog.nl(); return null;
						default: Dialog.important("Invalid input: " + key); break;
					}
				}
			} catch (Exception e) {
				
			}
		} else {
			if (ua.size() > 1) {
				// if We're below 50% attempt a heal
				if (p1.hp() <= p1.stamina() / 2) {
					ArrayList<Ability> a = ResourceStore.filter(ua, c -> (((Ability) c).hasEffect(HEAL)) );
					if (a.size() > 0) {
						ability = a.get(Utils.random(0, a.size()));
					}
				// Prefer avoiding heals
				} else {
					ArrayList<Ability> a = ResourceStore.filter(ua, c -> (!((Ability) c).hasEffect(HEAL)) );
					if (a.size() > 0) {
						// Keep up our buffs
						if (ResourceStore.filter(p1.buffs(), c -> ((Buff) c).isBuff()).size() == 0) {
							ArrayList<Ability> buffAbility = ResourceStore.filter(a, c -> ((Ability) c).hasBuff());
							if (buffAbility.size() > 0) a = buffAbility;
						}
						ability = a.get(Utils.random(0, a.size()));
					}
				}
				if (ability == null)
					ability = ua.get(Utils.random(0, ua.size()));
			} else if (ua.size() == 1)
				ability = ua.get(0);
			else
				ability = null;
		}
		this.dialog.nl();
		return ability;
	}
	/**
	 *  Does the work of what happens during the turn.
	 * 1: Gets an ability for the participant
	 * 2: Does buffs & Debuffs (start of turn)
	 * 3: deals or heals calculated damage or healing
	 * 4: Prints out the dialog
	 * 5: Does buffs & debuffs (end turn)
	 * 
	 * @param p1 current participant
	 * @param p2 opponent
	 * @return always returns false, unless the player runs. Then returns true.
	 */
	public boolean doTurn (Participant p1, Participant p2) {
		dialog.healing = 0;
		dialog.damage = 0;
		if (p1.isPlayer()) {
			this.turnCount++;
		}
		// Check if we lose our turn
		if (!doBuffs(p1, p2, true)) {
			if (p1.hp() <= 0 || p2.hp() <= 0) return false;
			
			Ability ability = getAbility(p1);
			if (ability == null) {
				if (p1.isPlayer()) // Player ran away.
					return true;
				dialog.e("%M is out of usable moves!");
				return false;
			}
	
			if (ability != null) {
				Ability.AbilityDialog dialog = ability.dialog();
				Ability.AbilityDialog buffDialog = null;
	
				if (ability.hasBuff())
					buffDialog = ability.buff().dialog();
				if (ability.hasDebuff())
					buffDialog = ability.debuff().dialog();
	
				if (ability.cooldown() > 0)
						ability.used();
				if (ability.chargeUp() > 0) {
					if (ability.charged()) {
						// OK to use ability!
						p1.isCharging = false;
					} else {
						// Not OK to use ability!
						p1.isCharging = true;
						this.dialog.println(dialog.charging);
					}
				}
	
				int status = doAttack(p1, p2, ability);
				p1.setMP(p1.mp()-ability.calcCost(p1.level()));
				
				if (hasCond(Battle.HEAL, status)) {
					if (p1.hp() + this.dialog.healing > p1.stamina())
						this.dialog.healing = (int) (p1.stamina() - p1.hp());
					p1.setHP(p1.hp()+this.dialog.healing);
					this.dialog.println(dialog.attack);
				}
				// if We've reflected damage, do it to ourself 
				// instead of damage to our opponent
				if (hasCond(Battle.REFLECT, status)) {
					p1.setHP(p1.hp()-this.dialog.damage);
					this.dialog.e(dialog.failed);
				} else if (hasCond(Battle.DAMAGE, status)) {
					p2.setHP(p2.hp()-this.dialog.damage);
					this.dialog.println(dialog.attack);
				}
				
				// Missed Dialog Conditions
				if (hasCond(Battle.MISS, status)) {
					this.dialog.e(dialog.missed);
				}
				if (hasCond(Battle.BUFFMISS, status) || hasCond(Battle.DEBUFFMISS, status)) {
					this.dialog.e(dialog.buffMissed);
				}
				
				// Buff Dialog
				if (hasCond(Battle.BUFF, status) || hasCond(Battle.DEBUFF, status)) {
					this.dialog.println(buffDialog.buff);
				}
				
				if (status == Battle.INEFFECTIVE) {
					if (p1.isPlayer()) this.dialog.dialog = "Your";
					else this.dialog.dialog = "%M's ";
					this.dialog.dialog += ability.name() + " was ineffective!"; 
					this.dialog.println(); 
				}
			}
		}
		// Even if we've lost our turn, we still could have died
		// Check before doing post turn Buffs
		if (p1.hp() <= 0 || p2.hp() <= 0) return false;

		this.doBuffs(p1, p2, false);
		return false;
	}
	
	/**
	 * Does the work of who's turn it is and who wins.
	 * @return Battle.WIN | Battle.LOSE | Battle.RAN
	 */
	public int battleStart() {
		ArrayList<Creature> died = new ArrayList<Creature>();
		do {
			this.paTurn += player.speed() / this.maxTurn;
			if (this.paTurn >= this.maxTurn) {
				this.paTurn = 0;
				if (this.doTurn(player, monsters.get(0))) {
					monsters.clear();
					return Battle.RAN;
				}
			}
			if (player.hp() <= 0) return Battle.LOSE;
			for (Creature monster: monsters) {
				monster.turnCount += monster.speed() / this.maxTurn;
				if (monster.hp() <= 0) died.add(monster);
			}
			if (died.size() > 0) {
				monsters.removeAll(died);
				died.clear();
			}
			if (monsters.size() == 0) return Battle.WIN;
			for (Creature monster: monsters) {
				if (monster.turnCount >= this.maxTurn) {
					monster.turnCount = 0;
					doTurn(monster, player);
					if (player.hp() <= 0) return Battle.LOSE;
					if (monster.hp() <= 0) died.add(monster);
				}
			}
			if (died.size() > 0) {
				monsters.removeAll(died);
				died.clear();
			}
		} while (monsters.size() > 0);
		return Battle.WIN;
	}
}
