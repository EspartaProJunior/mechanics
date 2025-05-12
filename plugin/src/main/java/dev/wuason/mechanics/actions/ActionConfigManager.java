package dev.wuason.mechanics.actions;

import dev.wuason.mechanics.actions.args.Argument;
import dev.wuason.mechanics.actions.args.Arguments;
import dev.wuason.mechanics.actions.config.*;
import dev.wuason.mechanics.actions.events.Events;
import dev.wuason.mechanics.actions.executators.Executor;
import dev.wuason.mechanics.actions.executators.Executors;
import dev.wuason.mechanics.actions.executators.Run;
import dev.wuason.mechanics.actions.utils.ActionConfigUtils;
import dev.wuason.mechanics.mechanics.MechanicAddon;
import dev.wuason.mechanics.utils.AdventureUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActionConfigManager {
    private MechanicAddon core;
    private ActionManager actionManager;

    public ActionConfigManager(MechanicAddon core, ActionManager actionManager) {
        this.core = core;
        this.actionManager = actionManager;
    }
    public void loadActions(File base){

        actionManager.clearActionConfigs();
        actionManager.registerAllEvents();

        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            try {
                FileInputStream fis = new FileInputStream(file);
                Reader reader = new InputStreamReader(fis);
                YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);

                ConfigurationSection sectionActions = config.getConfigurationSection("actions");

                if(sectionActions != null){
                    for(String key : sectionActions.getKeys(false)){

                        ConfigurationSection actionSection = sectionActions.getConfigurationSection((String)key);
                        if(actionSection == null) continue;
                        // EVENT CONFIG
                        String eventStr = actionSection.getString("event", "default").toUpperCase(Locale.ENGLISH);
                        if(!Events.EVENTS.containsKey(eventStr)){
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Event is invalid");
                            continue;
                        }

                        //RUN CONFIG
                        String runStr = actionSection.getString("run", "sync");
                        Run run = null;
                        try {
                            run = Run.valueOf(runStr.toUpperCase(Locale.ENGLISH));
                        }
                        catch (Exception e){
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: run method is invalid");
                            continue;
                        }

                        //EXECUTOR CONFIG
                        String executorStr = actionSection.getString("execute_as", "default").toUpperCase(Locale.ENGLISH);
                        if(!Executors.EXECUTORS.containsKey(executorStr)){
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Action Config! action_id: " + key +  " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Executator method is invalid");
                            continue;
                        }
                        Executor executor = Executors.EXECUTORS.get(executorStr);

                        //VAR LIST CONFIG
                        ArrayList<VarListConfig<?>> varListComputed = new ArrayList<>();
                        ConfigurationSection varsList = actionSection.getConfigurationSection("vars_list");
                        if(varsList != null){

                            for(String varListkey : varsList.getKeys(false)){

                                ConfigurationSection varList = varsList.getConfigurationSection(varListkey);

                                String var = varList.getString("var");
                                if(var == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Var is null or invalid!");
                                    continue;
                                }

                                Class<? extends Argument> argType = Arguments.ARGUMENTS.get(varList.getString("type").toUpperCase(Locale.ENGLISH));

                                if(argType == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Type is null or invalid!");
                                    continue;
                                }

                                List<String> argList = varList.getStringList("list");
                                if(argList == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var List Config! var_list_id: " + varListkey + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: List is null or invalid!");
                                    continue;
                                }

                                varListComputed.add(ActionConfigUtils.getVarList(var, argType, argList));
                            }

                        }
                        //VARS CONFIG
                        ArrayList<VarConfig> varsComputed = new ArrayList<>();

                        List<String> vars = actionSection.getStringList("vars");
                        if(vars != null){
                            for(String v : vars){
                                VarConfig varConfig = ActionConfigUtils.getVar(v);
                                if(varConfig==null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Var Config! var: " + v + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Var is null or invalid!");
                                    continue;
                                }
                                varsComputed.add(varConfig);
                            }
                        }
                        //FUNCTIONS CONFIG
                        ArrayList<FunctionConfig> functionsComputed = new ArrayList<>();
                        List<String> functions = actionSection.getStringList("functions");
                        if(functions != null){
                            for(String f : functions){
                                FunctionConfig functionConfig = ActionConfigUtils.getFunction(f);
                                if(functionConfig == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Function Config! function: " + f + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Function is null or invalid!");
                                    continue;
                                }
                                functionsComputed.add(functionConfig);
                            }
                        }
                        //CONDITIONS CONFIG
                        ArrayList<ConditionConfig> conditionsList = new ArrayList<>();
                        List<String> conditions = actionSection.getStringList("conditions");
                        if(conditions != null){
                            for(String c : conditions){
                                ConditionConfig conditionConfig = ActionConfigUtils.getCondition(c);
                                if(conditionConfig == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Function Config! condition: " + c + " action_id: " + key +  " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Condition is null or invalid!");
                                    continue;
                                }
                                conditionsList.add(conditionConfig);
                            }
                        }
                        List<String> importsList = actionSection.getStringList("java_imports");
                        if(importsList == null) importsList = new ArrayList<>();
                        ActionConfig actionConfig = new ActionConfig(importsList, run, executor, eventStr, key, varsComputed, varListComputed, functionsComputed, conditionsList);

                        actionManager.registerActionConfig(actionConfig);
                    }
                }

                fis.close();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
