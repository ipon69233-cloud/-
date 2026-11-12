package dev.nova.module;

import dev.nova.module.combat.AutoTotem;
import dev.nova.module.combat.Criticals;
import dev.nova.module.combat.KillAura;
import dev.nova.module.combat.Reach;
import dev.nova.module.combat.TriggerBot;
import dev.nova.module.combat.Velocity;
import dev.nova.module.movement.AirJump;
import dev.nova.module.movement.AutoWalk;
import dev.nova.module.movement.Fly;
import dev.nova.module.movement.HighJump;
import dev.nova.module.movement.Jesus;
import dev.nova.module.movement.NoFall;
import dev.nova.module.movement.NoSlow;
import dev.nova.module.movement.Speed;
import dev.nova.module.movement.Spider;
import dev.nova.module.movement.Sprint;
import dev.nova.module.movement.Step;
import dev.nova.module.player.AutoEat;
import dev.nova.module.player.AutoFish;
import dev.nova.module.player.AutoRespawn;
import dev.nova.module.player.AutoTool;
import dev.nova.module.player.ChestStealer;
import dev.nova.module.player.FastBreak;
import dev.nova.module.player.FastPlace;
import dev.nova.module.render.ClickGui;
import dev.nova.module.render.ESP;
import dev.nova.module.render.Fullbright;
import dev.nova.module.render.Hud;
import dev.nova.module.render.StorageESP;
import dev.nova.module.render.Tracers;
import dev.nova.module.render.Xray;
import dev.nova.module.render.Zoom;
import dev.nova.module.world.Nuker;
import dev.nova.module.world.Scaffold;
import dev.nova.module.world.Timer;

import java.util.List;

/** All modules. Mixins read the ones they need through these fields. */
public final class Modules {
	public static final KillAura KILL_AURA = new KillAura();
	public static final TriggerBot TRIGGER_BOT = new TriggerBot();
	public static final Criticals CRITICALS = new Criticals();
	public static final Velocity VELOCITY = new Velocity();
	public static final AutoTotem AUTO_TOTEM = new AutoTotem();
	public static final Reach REACH = new Reach();

	public static final Fly FLY = new Fly();
	public static final Speed SPEED = new Speed();
	public static final Sprint SPRINT = new Sprint();
	public static final NoFall NO_FALL = new NoFall();
	public static final Step STEP = new Step();
	public static final HighJump HIGH_JUMP = new HighJump();
	public static final AirJump AIR_JUMP = new AirJump();
	public static final Spider SPIDER = new Spider();
	public static final Jesus JESUS = new Jesus();
	public static final NoSlow NO_SLOW = new NoSlow();
	public static final AutoWalk AUTO_WALK = new AutoWalk();

	public static final Fullbright FULLBRIGHT = new Fullbright();
	public static final ESP ESP = new ESP();
	public static final Tracers TRACERS = new Tracers();
	public static final StorageESP STORAGE_ESP = new StorageESP();
	public static final Xray XRAY = new Xray();
	public static final Zoom ZOOM = new Zoom();
	public static final Hud HUD = new Hud();
	public static final ClickGui CLICK_GUI = new ClickGui();

	public static final AutoTool AUTO_TOOL = new AutoTool();
	public static final FastPlace FAST_PLACE = new FastPlace();
	public static final FastBreak FAST_BREAK = new FastBreak();
	public static final AutoEat AUTO_EAT = new AutoEat();
	public static final AutoRespawn AUTO_RESPAWN = new AutoRespawn();
	public static final ChestStealer CHEST_STEALER = new ChestStealer();
	public static final AutoFish AUTO_FISH = new AutoFish();

	public static final Nuker NUKER = new Nuker();
	public static final Scaffold SCAFFOLD = new Scaffold();
	public static final Timer TIMER = new Timer();

	public static final List<Module> ALL = List.of(
		KILL_AURA, TRIGGER_BOT, CRITICALS, VELOCITY, AUTO_TOTEM, REACH,
		FLY, SPEED, SPRINT, NO_FALL, STEP, HIGH_JUMP, AIR_JUMP, SPIDER, JESUS, NO_SLOW, AUTO_WALK,
		FULLBRIGHT, ESP, TRACERS, STORAGE_ESP, XRAY, ZOOM, HUD, CLICK_GUI,
		AUTO_TOOL, FAST_PLACE, FAST_BREAK, AUTO_EAT, AUTO_RESPAWN, CHEST_STEALER, AUTO_FISH,
		NUKER, SCAFFOLD, TIMER
	);

	private Modules() {
	}
}
