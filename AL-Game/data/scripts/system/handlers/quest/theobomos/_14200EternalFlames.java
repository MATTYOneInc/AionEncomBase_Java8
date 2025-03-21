/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package quest.theobomos;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestDialog;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author FrozenKiller
 */
public class _14200EternalFlames extends QuestHandler {

    private final static int questId = 14200;
    public _14200EternalFlames() {
        super(questId);
    }

    @Override
    public void register() {
        qe.registerQuestNpc(798155).addOnQuestStart(questId); // Atropos
        qe.registerQuestNpc(798155).addOnTalkEvent(questId); // Atropos
        qe.registerQuestNpc(798206).addOnTalkEvent(questId); // Josnack
        qe.registerQuestNpc(700390).addOnKillEvent(questId); // Eternal Flame
    }

    @Override
    public boolean onDialogEvent(final QuestEnv env) {
        final Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestDialog dialog = env.getDialog();
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 798155) { // Atropos
                if (dialog == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 1011);
                }
                else {
                    return sendQuestStartDialog(env);
                }
            }
        }
        else if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (targetId == 798206) { // Josnack
                switch (dialog) {
                    case START_DIALOG: {
                        if (var == 0) {
                            return sendQuestDialog(env, 1352);
                        }
                    }
                    case STEP_TO_1: {
                        qs.setQuestVar(0);
                        updateQuestStatus(env);
                        updateQuestStatus(env); // That's not an misstake it's needed for the Quest Update Sound
                        return closeDialogWindow(env);
                    }
                    default:
                        break;
                }
            }
            else if (targetId == 798155) { // Atropos
                switch (dialog) {
                    case START_DIALOG: {
                        if (var == 3) {
                            return sendQuestDialog(env, 2375);
                        }
                    }
                    case SELECT_REWARD: {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestDialog(env, 5);
                    }
                    default:
                        break;
                }
            }
        }
        else if (qs == null || qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 798155) { // Atropos
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) {
            return false;
        }
		int targetId = env.getTargetId();
        int var = qs.getQuestVarById(0);
        if (var >= 0 && var < 3) {
            return defaultOnKillEvent(env, 700390, var, var + 1); // 0 - 3
        }
        else {
            return false;
        }
    }
}