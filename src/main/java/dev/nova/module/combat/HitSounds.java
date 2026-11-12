package dev.nova.module.combat;

import dev.nova.module.Category;
import dev.nova.module.Module;
import dev.nova.setting.ModeSetting;
import dev.nova.setting.NumberSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class HitSounds extends Module {
	private final ModeSetting sound = mode("Звук", "Опыт", "Опыт", "Колокол", "Пузырь");
	private final NumberSetting volume = number("Громкость", 60, 0, 100, 1);

	public HitSounds() {
		super("HitSounds", "Звук при попадании по цели", Category.COMBAT);
	}

	/** Called right before the attack packet is sent. */
	public void onAttack(Entity target) {
		if (!(target instanceof LivingEntity)) return;
		SoundEvent event;
		float pitch = 1f;
		if (sound.is("Колокол")) {
			event = SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
			pitch = 1.3f;
		} else if (sound.is("Пузырь")) {
			event = SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP;
		} else {
			event = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
		}
		mc.getSoundManager().play(PositionedSoundInstance.ui(event, pitch, volume.getFloat() / 100f));
	}
}
