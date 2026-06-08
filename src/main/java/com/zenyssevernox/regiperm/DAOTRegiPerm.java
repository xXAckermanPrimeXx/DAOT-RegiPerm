package com.zenyssevernox.regiperm;

import net.fabricmc.api.ModInitializer;
import com.zenyssevernox.regiperm.event.RegiPermChatEvents;
import com.zenyssevernox.regiperm.command.RegiPermCommands;
import com.zenyssevernox.regiperm.event.RegiPermClaimEvents;
import com.zenyssevernox.regiperm.event.RegiPermLoreEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOTRegiPerm implements ModInitializer {
	public static final String MOD_ID = "regiperm";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("RegiPerm is loading...");
		RegiPermCommands.register();
		RegiPermChatEvents.register();
		RegiPermClaimEvents.register();
		RegiPermLoreEvents.register();
	}
}