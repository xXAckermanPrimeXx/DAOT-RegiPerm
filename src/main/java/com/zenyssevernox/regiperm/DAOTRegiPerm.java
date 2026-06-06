package com.zenyssevernox.regiperm;

import net.fabricmc.api.ModInitializer;
 import com.zenyssevernox.regiperm.command.RegiPermCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOTRegiPerm implements ModInitializer {
	public static final String MOD_ID = "regiperm";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("RegiPerm is loading...");
		RegiPermCommands.register();
	}
}