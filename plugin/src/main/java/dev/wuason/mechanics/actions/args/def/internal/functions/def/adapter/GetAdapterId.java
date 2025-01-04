package dev.wuason.mechanics.actions.args.def.internal.functions.def.adapter;

import dev.wuason.adapter.Adapter;
import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.args.def.internal.functions.FunctionInternal;
import dev.wuason.mechanics.actions.args.def.internal.functions.FunctionInternalArgument;
import dev.wuason.mechanics.actions.args.def.internal.functions.FunctionInternalArgumentProperties;
import dev.wuason.mechanics.actions.args.def.internal.functions.FunctionInternalProperties;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GetAdapterId extends FunctionInternal {

    public static final String ID = "getAdapterId";

    public static final FunctionInternalProperties PROPERTIES = new FunctionInternalProperties.Builder().build();

    public static final HashMap<String, FunctionInternalArgument> ARGS = new HashMap<String, FunctionInternalArgument>(){{

        FunctionInternalArgument item = new FunctionInternalArgument("item", 0, new FunctionInternalArgumentProperties.Builder().build()) {
            @Override
            public Object computeArg(String line, Action action, Object... args) {
                return null;
            }
        };

        put(item.getName(), item);

    }};

    public GetAdapterId() {
        super(PROPERTIES, ARGS, "getAdapterId");
    }

    @Override
    public Object compute(Action action, Object... args) {
        if(args[0] == null) return null;
        return Adapter.getAdapterId((ItemStack) args[0]);
    }
}
