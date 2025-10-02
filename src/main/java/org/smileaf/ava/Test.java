package org.smileaf.ava;

import java.util.ArrayList;

import org.smileaf.game.Ability;
import org.smileaf.game.Creature;
import org.smileaf.game.Item;
import org.smileaf.game.Job;
import org.smileaf.game.Resource;
import org.smileaf.game.Player;
import org.smileaf.game.Dialog;

public class Test {
	private Player player;
	private Creature mon;
	private Ability ab;
	private Job job;

	public Test() {
	}
	public void output(Ability ab) {
		display(ab, false);
		if (ab.hasDebuff() && ab.debuff().hasEffect(Ability.DAMAGE)) {
			System.out.println("Debuff: "+ab.debuff().name());
			display(ab, true);
		}
	}
	public Player loadPlayer(String name) {
		player = new Player(name, App.resource);
		player.silent = true;
		App.dialog.setPlayer(player);

		// Set Default Job
		player.setJob((Job)App.resource.getResourcesByNameAndType("Freelancer", Resource.JOB).get(0));

		// Set default gear
		//player.equip((Item)App.resource.getResourcesByNameAndType("Wood Sword", Resource.ITEM).get(0));
		return player;
	}

	public Creature loadCreature() {
		System.out.println("Creatures:");
		ArrayList<Resource> cl = App.resource.getResourcesByType(Resource.CREATURE);
		for (int i=0;i<cl.size();i++) {
			System.out.printf("%3d: %2$s\n", i,cl.get(i));
		}
		do {
			String cr = App.dialog.getInputString("Load Creature: ");
			mon = (Creature) cl.get(Integer.parseInt(cr));
			if (mon == null) App.dialog.e("Error loading creature.");
		} while (mon == null);
		return mon;
	}
	public Ability loadAbility(Creature mon) {
		System.out.println("Abilities:");
		for (int i=0;i<mon.ability().size();i++) {
			System.out.printf("%3d: %2$s\n", i, mon.ability().get(i).name());
		}
		do {
			String la = App.dialog.getInputString("Load Ability: ");
			ab = mon.ability().get(Integer.parseInt(la));
			if (ab == null) App.dialog.e("Error Loading ability.");
		} while (ab == null);
		return ab;
	}
	public Job loadJob() {
		App.dialog.println("Jobs:");
		ArrayList<Resource> j = App.resource.getResourcesByType(Resource.JOB);
		for (int i=0;i<j.size();i++) {
			System.out.printf("%3d: %2$s\n", i,j.get(i));
		}
		do {
			String cr = App.dialog.getInputString("Load Job: ");
			job = (Job) j.get(Integer.parseInt(cr));
			if (job == null) App.dialog.e("Error loading Job: " + cr);
		} while (job == null);
		return job;
	}
	public Item loadEquipment() {
		App.dialog.println("Equipment:");
		ArrayList<Resource> i = App.resource.getResourcesByType(Resource.ITEM);
		Item item = null;
		for (int r=0;r<i.size();r++) {
			item = (Item)i.get(r);
			if (item.itemType() == Item.WEAPON || item.itemType() == Item.ARMOR)
				System.out.printf("%3d: %2$s\n", r,i.get(r));
		}
		do {
			String l = App.dialog.getInputString("Load Item: ");
			if (l.substring(0, 1).equals("-")) {
				l = l.substring(1);
				item = (Item) i.get(Integer.parseInt(l));
				player.unEquip(item);
				return null;
			}
			item = (Item) i.get(Integer.parseInt(l));
			if (item == null) App.dialog.e("Error loading Item: " + l);
		} while (item == null);
		return item;
	}

	public void test() {
		this.player = loadPlayer("Test");
		this.mon = loadCreature();
		ab = loadAbility(mon);
		output(ab);
		float stat;
		int power;
		float scaling;
		while (true) {
			App.dialog.println("(C)reature (A)bility (J)ob (E)quip (D)isplay (Q)uit");
			App.dialog.println("Ability[Ability: 1; Debuff: 2]: (P)ower (S)caling");
			App.dialog.println("Monster[Base: 1; Scaling: 2]: A(t)tack (Sp)eed (M)agic");
			String c = App.dialog.getInputString("Command: ");
			try  {
				switch (c.toLowerCase()) {
				case "c": loadCreature(); break;
				case "a":
					output(loadAbility(mon));
					break;
				case "j": player.setJob(loadJob()); break;
				case "s":
					player.isPlayer(false);
					int w = 0;
					int l = 0;
					//App.dialog.silent = true;
					player.silent = true;
					int low = mon.levels()[0] * 2 -1;
					int high = 2; //mon.levels()[1] * 2 + 1;
					for (int lvl = low; lvl <= high; lvl++) {
						System.out.print("Lvl: " + lvl);
						int lw = 0;
						int ll = 0;
						for (int bs = 0; bs < 2; bs++) {
							player.setLevel(lvl);
							player.heal();
							Battle b = new Battle(App.dialog, player);
							b.addCreature(mon, 1);
							mon.setLevel(lvl);
							int s = b.battleStart();
							if (s == Battle.WIN) {
								lw++;
							} else if (s == Battle.LOSE) {
								ll++;
							}
						}
						w += lw;
						l += ll;
						System.out.println(" W: "+lw+" L: "+ll);
					}
					//App.dialog.silent = false;
					player.silent = false;
					App.dialog.println("Total: Wins: " + w + " Losses: " + l);
					break;
				case "e":
					Item equip = loadEquipment();
					player.equip(equip);
					break;
				case "p1":
					power = Integer.parseInt(App.dialog.getInputString("Ability Power (Current: "+ab.getPower()+"): "));
					ab.setPower(power);
					break;
				case "s1":
					scaling = Float.parseFloat(App.dialog.getInputString("Ability Scaling (Current: "+ab.getScaling()+"): "));
					ab.setScaling(scaling);
					break;
				case "p2":
					power = Integer.parseInt(App.dialog.getInputString("Debuff Power (Current: "+ab.debuff().getPower()+"): "));
					ab.debuff().setPower(power);
					break;
				case "s2":
					scaling = Float.parseFloat(App.dialog.getInputString("Debuff Scaling (Current: "+ab.debuff().getScaling()+"): "));
					ab.debuff().setScaling(scaling);
					break;
				case "t1":
					stat = Float.parseFloat(App.dialog.getInputString("Attack Base (Current: "+mon.stats().attack+"): "));
					mon.stats().attack = stat;
					break;
				case "t2":
					stat = Float.parseFloat(App.dialog.getInputString("Attack Scaling (Current: "+mon.lStats.attack+"): "));
					mon.lStats.attack = stat;
					break;
				case "sp1":
					stat = Float.parseFloat(App.dialog.getInputString("Speed Base (Current: "+mon.stats().speed+"): "));
					mon.stats().speed = stat;
					break;
				case "sp2":
					stat = Float.parseFloat(App.dialog.getInputString("Speed Scaling (Current: "+mon.lStats.speed+"): "));
					mon.lStats.speed = stat;
					break;
				case "m1":
					stat = Float.parseFloat(App.dialog.getInputString("Magic Base: (Current: " + mon.stats().magic+"): "));
					mon.stats().magic = stat;
					break;
				case "m2":
					stat = Float.parseFloat(App.dialog.getInputString("Magic Scaling: (Current: " + mon.lStats.magic+"): "));
					mon.lStats.magic = stat;
					break;
				case "d":
					output(ab);
					break;
				case "q": System.exit(0); break;
				default: App.dialog.e("Bad input"); break;
				}
			} catch (NumberFormatException e) {
				App.dialog.e("Bad input");
			}
		}
	}

	public void display(Ability ab, boolean debuff) {
		this.player.setLevel(1);
		float power = (debuff)?ab.debuff().getPower():ab.getPower();
		float scaling = (debuff)?ab.debuff().getScaling():ab.getScaling();
		int acc = (int) ((debuff)?ab.debuff().getAccuracy(Ability.DEBUFF):ab.getAccuracy(Ability.NORMAL));
		String dialog = "Name: " + ab.name();
		if (debuff) dialog += "(debuff)";
		App.dialog.text(dialog).println();
		dialog = "Power: "+power+" Scaling: "+scaling+" Accuracy: "+acc;
		boolean physical = false;
		if (debuff) {
			dialog += " Duration: "+ab.debuff().duration();
			if (ab.debuff().isPhysical()) {
				physical=true;
			}
		} else {
			if (ab.isPhysical()) {
				physical=true;
			}
		}
		App.dialog.text(dialog).println();
		Dialog.p("Lv: ");
		if (physical) Dialog.p("mAt   pDf");
		else Dialog.p("mMg   pMg");
		Dialog.pln("   Dmg   DDg   pHP   mHP   mSp  pSp  Turn   mMP   pMP  MP  TTK/D");

		//int level = Utils.random(floor * 2 - 1, floor * 2 + 1);
		int low = mon.levels()[0] * 2 -1;
		int high = mon.levels()[1] * 2 + 1;
		for (int i=1;i<low;i++) player.levelUp();

		for (int l=low; l<=high; l++) {
			float att;
			mon.setLevel(l);
			if (physical)
				att = mon.attack() * scaling + power;
			else
				att = mon.magic() * scaling + power;
			float def = (physical)?player.defense():player.magicDefense();
			float turn = mon.speed() / player.speed();
			int dmg = (debuff)?(int)Battle.doDebuffDamage(mon, player, ab.debuff()):Battle.doDamage(att, def);
			int dDmg = (debuff)?(int)Battle.doDebuffDamage(mon, player, ab.debuff()):Battle.doDamage(att, def*1.5f);
			System.out.printf("%2d: %3d  %4d   %3d   %3d   %3d   %3d   %3d  %3d   %1.1f   %3d   %3d %3d  %1.1f/%1.1f\n",
					mon.level(),
					(int)att,
					(int)def,
					dmg,
					dDmg,
					(int)player.stamina(),
					(int)mon.stamina(),
					(int)mon.speed(),
					(int)player.speed(),
					turn,
					(int)mon.magic(),
					(int)player.magic(),
					ab.calcCost(mon.level()),
					player.stamina()/dmg,
					player.stamina()/dDmg);
			player.levelUp();
		}
	}
}
