package org.pokesplash.hunt.config;

import com.google.gson.Gson;
import org.pokesplash.hunt.Hunt;
import org.pokesplash.hunt.util.Utils;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Config file.
 */
public class Config {
	private boolean individualHunts; // if hunts should be individual for each player.
	private boolean sendHuntEndMessage; // Should the mod send a message when a hunt ends.
	private boolean sendHuntBeginMessage; // Should the mod send a message when a hunt begins.
	private int huntDuration; // How long each hunt should last, in minutes.
	private int huntAmount; // How many hunts should there be at once.
	private RarityConfig rarity; // The rarity borders.
	private RewardsConfig rewards; // The rewards for the hunts.
	private Properties matchProperties; // What properties should be checked to complete the hunt.
	private ArrayList<CustomPrice> customPrices; // List of custom prices.
	private ArrayList<String> blacklist; // List if Pokemon that shouldn't be added to Hunt.

	public Config() {
		individualHunts = false;
		sendHuntEndMessage = true;
		sendHuntBeginMessage = true;
		huntDuration = 60;
		huntAmount = 7;
		rarity = new RarityConfig();
		rewards = new RewardsConfig();
		matchProperties = new Properties();
		customPrices = new ArrayList<>();
		customPrices.add(new CustomPrice());
		blacklist = new ArrayList<>();
	}

	/**
	 * Reads the config or writes one if a config doesn't exist.
	 */
	public void init() {
		CompletableFuture<Boolean> futureRead = Utils.readFileAsync("/config/hunt/", "config.json",
				el -> {
					Gson gson = Utils.newGson();
					Config cfg = gson.fromJson(el, Config.class);
					huntDuration = cfg.getHuntDuration();

					if (cfg.getHuntAmount() > 28) {
						huntAmount = 28;
						Hunt.LOGGER.error("Hunt amount can not be higher than 28");
					} else {
						huntAmount = cfg.getHuntAmount();
					}
					individualHunts = cfg.isIndividualHunts();
					sendHuntEndMessage = cfg.isSendHuntEndMessage();
					sendHuntBeginMessage = cfg.isSendHuntBeginMessage();
					matchProperties = cfg.getMatchProperties();
					customPrices = cfg.getCustomPrices();
					blacklist = cfg.getBlacklist();
					rarity = cfg.getRarity();
					rewards = cfg.getRewards();
				});

		// If the config couldn't be read, write a new one.
		if (!futureRead.join()) {
			Hunt.LOGGER.info("No config.json file found for Hunt. Attempting to generate one.");
			Gson gson = Utils.newGson();
			String data = gson.toJson(this);
			CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync("/config/hunt/", "config.json",
					data);

			// If the write failed, log fatal.
			if (!futureWrite.join()) {
				Hunt.LOGGER.fatal("Could not write config for Hunt.");
			}
			return;
		}
		Hunt.LOGGER.info("Hunt config file read successfully.");
	}


	/**
	 * Bunch of Getters
	 */

	public boolean isIndividualHunts() {
		return individualHunts;
	}

	public int getHuntDuration() {
		return huntDuration;
	}

	public int getHuntAmount() {
		return huntAmount;
	}


	public Properties getMatchProperties() {
		return matchProperties;
	}

	public ArrayList<CustomPrice> getCustomPrices() {
		return customPrices;
	}

	public boolean isSendHuntEndMessage() {
		return sendHuntEndMessage;
	}

	public boolean isSendHuntBeginMessage() {
		return sendHuntBeginMessage;
	}

	public ArrayList<String> getBlacklist() {
		return blacklist;
	}

	public RarityConfig getRarity() {
		return rarity;
	}

	public RewardsConfig getRewards() {
		return rewards;
	}

	public boolean blacklistContains(String pokemon) {
		for (String name : blacklist) {
			if (name.equalsIgnoreCase(pokemon)) return true;
		}
		return false;
	}
}
