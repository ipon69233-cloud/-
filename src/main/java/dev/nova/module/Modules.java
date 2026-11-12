package dev.nova.module;

import dev.nova.module.combat.AutoGApple;
import dev.nova.module.combat.AutoTotem;
import dev.nova.module.combat.BowHelper;
import dev.nova.module.combat.Criticals;
import dev.nova.module.combat.HitAura;
import dev.nova.module.combat.HitBox;
import dev.nova.module.combat.HitSounds;
import dev.nova.module.combat.Reach;
import dev.nova.module.combat.TriggerBot;
import dev.nova.module.combat.Velocity;
import dev.nova.module.movement.AirJump;
import dev.nova.module.movement.AirStuck;
import dev.nova.module.movement.AutoWalk;
import dev.nova.module.movement.DragonFly;
import dev.nova.module.movement.Fly;
import dev.nova.module.movement.HighJump;
import dev.nova.module.movement.InvMove;
import dev.nova.module.movement.Jesus;
import dev.nova.module.movement.NoFall;
import dev.nova.module.movement.NoSlow;
import dev.nova.module.movement.NoWeb;
import dev.nova.module.movement.Speed;
import dev.nova.module.movement.Spider;
import dev.nova.module.movement.Sprint;
import dev.nova.module.movement.Step;
import dev.nova.module.movement.WaterSpeed;
import dev.nova.module.player.AntiAFK;
import dev.nova.module.player.AutoEat;
import dev.nova.module.player.AutoFish;
import dev.nova.module.player.AutoRespawn;
import dev.nova.module.player.AutoTool;
import dev.nova.module.player.Blink;
import dev.nova.module.player.CameraClip;
import dev.nova.module.player.ChestStealer;
import dev.nova.module.player.ElytraHelper;
import dev.nova.module.player.FakePlayer;
import dev.nova.module.player.FastBreak;
import dev.nova.module.player.FreeCamera;
import dev.nova.module.player.MiddleClick;
import dev.nova.module.player.NoDelay;
import dev.nova.module.player.NoPush;
import dev.nova.module.render.Arrows;
import dev.nova.module.render.BlockESP;
import dev.nova.module.render.BlockOutline;
import dev.nova.module.render.ChinaHat;
import dev.nova.module.render.ClickGui;
import dev.nova.module.render.ESP;
import dev.nova.module.render.GlowESP;
import dev.nova.module.render.Hud;
import dev.nova.module.render.JumpCircle;
import dev.nova.module.render.NameTags;
import dev.nova.module.render.NightVision;
import dev.nova.module.render.StorageESP;
import dev.nova.module.render.TargetESP;
import dev.nova.module.render.Tracers;
import dev.nova.module.render.Trails;
import dev.nova.module.render.Xray;
import dev.nova.module.render.Zoom;
import dev.nova.module.world.Nuker;
import dev.nova.module.world.Removals;
import dev.nova.module.world.Scaffold;
import dev.nova.module.world.SeeInvisibles;
import dev.nova.module.world.TapeMouse;
import dev.nova.module.world.Timer;

import java.util.List;

/** All modules. Mixins read the ones they need through these fields. */
public final class Modules {
	public static final HitAura HIT_AURA = new HitAura();
	public static final TriggerBot TRIGGER_BOT = new TriggerBot();
	public static final AutoTotem AUTO_TOTEM = new AutoTotem();
	public static final Velocity VELOCITY = new Velocity();
	public static final Criticals CRITICALS = new Criticals();
	public static final Reach REACH = new Reach();
	public static final HitBox HIT_BOX = new HitBox();
	public static final HitSounds HIT_SOUNDS = new HitSounds();
	public static final BowHelper BOW_HELPER = new BowHelper();
	public static final AutoGApple AUTO_G_APPLE = new AutoGApple();

	public static final Sprint SPRINT = new Sprint();
	public static final Speed SPEED = new Speed();
	public static final NoSlow NO_SLOW = new NoSlow();
	public static final Jesus JESUS = new Jesus();
	public static final Spider SPIDER = new Spider();
	public static final InvMove INV_MOVE = new InvMove();
	public static final DragonFly DRAGON_FLY = new DragonFly();
	public static final WaterSpeed WATER_SPEED = new WaterSpeed();
	public static final AirStuck AIR_STUCK = new AirStuck();
	public static final NoWeb NO_WEB = new NoWeb();
	public static final Fly FLY = new Fly();
	public static final NoFall NO_FALL = new NoFall();
	public static final Step STEP = new Step();
	public static final HighJump HIGH_JUMP = new HighJump();
	public static final AirJump AIR_JUMP = new AirJump();
	public static final AutoWalk AUTO_WALK = new AutoWalk();

	public static final ESP ESP = new ESP();
	public static final GlowESP GLOW_ESP = new GlowESP();
	public static final TargetESP TARGET_ESP = new TargetESP();
	public static final NameTags NAME_TAGS = new NameTags();
	public static final Tracers TRACERS = new Tracers();
	public static final StorageESP STORAGE_ESP = new StorageESP();
	public static final BlockESP BLOCK_ESP = new BlockESP();
	public static final BlockOutline BLOCK_OUTLINE = new BlockOutline();
	public static final Xray XRAY = new Xray();
	public static final NightVision NIGHT_VISION = new NightVision();
	public static final ChinaHat CHINA_HAT = new ChinaHat();
	public static final JumpCircle JUMP_CIRCLE = new JumpCircle();
	public static final Trails TRAILS = new Trails();
	public static final Arrows ARROWS = new Arrows();
	public static final Zoom ZOOM = new Zoom();
	public static final Hud HUD = new Hud();
	public static final ClickGui CLICK_GUI = new ClickGui();

	public static final FreeCamera FREE_CAMERA = new FreeCamera();
	public static final AutoTool AUTO_TOOL = new AutoTool();
	public static final FastBreak FAST_BREAK = new FastBreak();
	public static final NoDelay NO_DELAY = new NoDelay();
	public static final NoPush NO_PUSH = new NoPush();
	public static final Blink BLINK = new Blink();
	public static final AntiAFK ANTI_AFK = new AntiAFK();
	public static final CameraClip CAMERA_CLIP = new CameraClip();
	public static final ElytraHelper ELYTRA_HELPER = new ElytraHelper();
	public static final MiddleClick MIDDLE_CLICK = new MiddleClick();
	public static final FakePlayer FAKE_PLAYER = new FakePlayer();
	public static final AutoFish AUTO_FISH = new AutoFish();
	public static final AutoEat AUTO_EAT = new AutoEat();
	public static final AutoRespawn AUTO_RESPAWN = new AutoRespawn();
	public static final ChestStealer CHEST_STEALER = new ChestStealer();

	public static final Scaffold SCAFFOLD = new Scaffold();
	public static final Nuker NUKER = new Nuker();
	public static final Timer TIMER = new Timer();
	public static final SeeInvisibles SEE_INVISIBLES = new SeeInvisibles();
	public static final Removals REMOVALS = new Removals();
	public static final TapeMouse TAPE_MOUSE = new TapeMouse();

	public static final List<Module> ALL = List.of(
		HIT_AURA, TRIGGER_BOT, AUTO_TOTEM, VELOCITY, CRITICALS, REACH, HIT_BOX, HIT_SOUNDS, BOW_HELPER, AUTO_G_APPLE,
		SPRINT, SPEED, NO_SLOW, JESUS, SPIDER, INV_MOVE, DRAGON_FLY, WATER_SPEED, AIR_STUCK, NO_WEB,
		FLY, NO_FALL, STEP, HIGH_JUMP, AIR_JUMP, AUTO_WALK,
		ESP, GLOW_ESP, TARGET_ESP, NAME_TAGS, TRACERS, STORAGE_ESP, BLOCK_ESP, BLOCK_OUTLINE, XRAY, NIGHT_VISION,
		CHINA_HAT, JUMP_CIRCLE, TRAILS, ARROWS, ZOOM, HUD, CLICK_GUI,
		FREE_CAMERA, AUTO_TOOL, FAST_BREAK, NO_DELAY, NO_PUSH, BLINK, ANTI_AFK, CAMERA_CLIP, ELYTRA_HELPER,
		MIDDLE_CLICK, FAKE_PLAYER, AUTO_FISH, AUTO_EAT, AUTO_RESPAWN, CHEST_STEALER,
		SCAFFOLD, NUKER, TIMER, SEE_INVISIBLES, REMOVALS, TAPE_MOUSE
	);

	private Modules() {
	}
}
