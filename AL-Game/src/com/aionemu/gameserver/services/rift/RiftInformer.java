/*

 *
 *  Encom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Encom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser Public License
 *  along with Encom.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.rift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIFT_ANNOUNCE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastMap;

/****/
/* Rework: sunbsn
 * Author Rinzler (Encom) /
 ****/

public class RiftInformer {
	public static List<Npc> getSpawned(int worldId) {
		// 使用CopyOnWriteArrayList来保证线程安全 Use CopyOnWriteArrayList to ensure thread safety
		List<Npc> rifts = new ArrayList<Npc>(RiftManager.getSpawned());  // 创建一个副本 Create a copy
		List<Npc> worldRifts = new CopyOnWriteArrayList<Npc>();
		
		synchronized(rifts) {  // 添加同步块 Add synchronization block
			for (Npc rift : rifts) {
				if (rift.getWorldId() == worldId) {
					worldRifts.add(rift);
				}
			}
		}
		return worldRifts;
	}

	public static void sendRiftsInfo(int worldId) {
		syncRiftsState(worldId, getPackets(worldId));
		int twinId = getTwinId(worldId);
		if (twinId > 0) {
			syncRiftsState(twinId, getPackets(twinId));
		}
	}

	public static void sendRiftsInfo(Player player) {
		syncRiftsState(player, getPackets(player.getWorldId()));
		int twinId = getTwinId(player.getWorldId());
		if (twinId > 0) {
			syncRiftsState(twinId, getPackets(twinId));
		}
	}

	public static void sendRiftInfo(int[] worlds) {
		for (int worldId : worlds) {
			syncRiftsState(worldId, getPackets(worlds[0], -1));
		}
	}

	public static void sendRiftDespawn(int worldId, int objId) {
		syncRiftsState(worldId, getPackets(worldId, objId), true);
	}

	private static List<AionServerPacket> getPackets(int worldId) {
		return getPackets(worldId, 0);
	}

	private static List<AionServerPacket> getPackets(int worldId, int objId) {
		List<AionServerPacket> packets = new ArrayList<AionServerPacket>();
		if (objId == -1) {
			for (Npc rift : getSpawned(worldId)) {
				RVController controller = (RVController) rift.getController();
				if (!controller.isMaster()) {
					continue;
				}
				packets.add(new SM_RIFT_ANNOUNCE(controller, false));
			}
		} else if (objId > 0) {
			packets.add(new SM_RIFT_ANNOUNCE(objId));
		} else {
			packets.add(new SM_RIFT_ANNOUNCE(getAnnounceData(worldId)));
			for (Npc rift : getSpawned(worldId)) {
				RVController controller = (RVController) rift.getController();
				if (!controller.isMaster()) {
					continue;
				}
				packets.add(new SM_RIFT_ANNOUNCE(controller, true));
				packets.add(new SM_RIFT_ANNOUNCE(controller, false));
			}
		}
		return packets;
	}

	private static void syncRiftsState(Player player, final List<AionServerPacket> packets) {
		for (AionServerPacket packet : packets) {
			PacketSendUtility.sendPacket(player, packet);
		}
	}

	private static void syncRiftsState(int worldId, final List<AionServerPacket> packets) {
		syncRiftsState(worldId, packets, false);
	}

	private static void syncRiftsState(int worldId, final List<AionServerPacket> packets, final boolean isDespawnInfo) {
		World.getInstance().getWorldMap(worldId).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				syncRiftsState(player, packets);
			}
		});
	}

	private static FastMap<Integer, Integer> getAnnounceData(int worldId) {
		FastMap<Integer, Integer> localRifts = new FastMap<Integer, Integer>();
		for (int i = 0; i < 8; i++) {
			localRifts.put(i, 0);
		}
		
		List<Npc> spawned = new ArrayList<Npc>(getSpawned(worldId));  // 创建副本 Create a copy
		synchronized(spawned) {  // 添加同步块 Add synchronization block
			for (Npc rift : spawned) {
				RVController rc = (RVController) rift.getController();
				localRifts = calcRiftsData(rc, localRifts);
			}
		}
		return localRifts;
	}

	private static FastMap<Integer, Integer> calcRiftsData(RVController rift, FastMap<Integer, Integer> local) {
		if (rift.isMaster()) {
			local.putEntry(0, local.get(0) + 1);
			if (rift.isVortex()) {
				local.putEntry(1, local.get(1) + 1);
			}
			local.putEntry(2, local.get(2) + 1);
			local.putEntry(3, local.get(3) + 1);
			local.putEntry(4, local.get(4) + 1);
		} else {
			local.putEntry(5, local.get(5) + 1);
			local.putEntry(6, local.get(6) + 1);
			if (rift.isVortex()) {
				local.putEntry(7, local.get(7) + 1);
			}
		}
		return local;
	}

	public List<Npc> getSpawned() {
		List<Npc> spawned = new ArrayList<Npc>();
		// 使用 RiftManager 获取 rifts 列表 Use RiftManager to get the list of rifts
		List<Npc> rifts = RiftManager.getSpawned();  
		for (Npc npc : rifts) {
			// 使用 npc 的方法检查是否已死亡 Use npc's method to check if it is already dead
			if (npc != null && !npc.getLifeStats().isAlreadyDead()) {  
				spawned.add(npc);
			}
		}
		return spawned;
	}

	private static int getTwinId(int worldId) {
		switch (worldId) {
		/**
		 * Elyos
		 */
		case 110070000: // Kaisinel Academy -> Brusthonin
			return 220050000;
		case 210020000: // Eltnen -> Morheim
			return 220020000;
		case 210040000: // Heiron -> Beluslan
			return 220040000;
		case 210130000: // Inggison [Master Server] -> Gelkmaros [Master Server]
			return 220140000;
		case 210070000: // Cygnea -> Enshar
			return 220080000;
		case 210060000: // Theobomos -> Marchutan Priory
			return 120080000;
		case 210100000: // Iluma -> Norsvold
			return 220110000;
		/**
		 * Asmodians
		 */
		case 120080000: // Marchutan Priory -> Theobomos
			return 210060000;
		case 220020000: // Morheim -> Eltnen
			return 210020000;
		case 220040000: // Beluslan -> Heiron
			return 210040000;
		case 220050000: // Brusthonin -> Kaisinel Academy
			return 110070000;
		case 220140000: // Gelkmaros [Master Server] -> Inggison [Master Server]
			return 210130000;
		case 220080000: // Enshar -> Cygnea
			return 210070000;
		case 220110000: // Norsvold -> Iluma
			return 210100000;
		default:
			return 0;
		}
	}
}