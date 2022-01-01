package io.pulsarlabs.spicyholograms.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import io.pulsarlabs.spicyholograms.SpicyHolograms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.logging.Logger;


public class SpicyCommandManager {
    private static final Function<Exception, Component> INVALID_SYNTAX_FUNCTION = e -> MiniMessage.miniMessage().parse("<#ff0000>Invalid Syntax! Use <#8ec3cf>" +
            String.format("/%s", ((InvalidSyntaxException) e).getCorrectSyntax()).split(" ")[0] + " help <#ff0000>for available commands!");
    private static final Function<Exception, Component> NO_PERMISSION_FUNCTION = e -> MiniMessage.miniMessage().parse
            ("<#ff0000>You do not have permission to do that! You are missing the permission node: <#8ec3cf>" + ((NoPermissionException) e).getMissingPermission() + "<#ff0000>.");

    private final Logger logger;
    private PaperCommandManager<CommandSender> commandManager;
    private AnnotationParser<CommandSender> annotationParser;

    public SpicyCommandManager(SpicyHolograms plugin) {
        this.logger = plugin.getLogger();

        try {
            this.commandManager = new PaperCommandManager<>(plugin,
                    AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build(),
                    Function.identity(), Function.identity());

            try {
                commandManager.registerBrigadier();
            } catch (Exception e) {
                logger.severe("Failed to register brigadier: " + e);
            }

            commandManager.setSetting(CommandManager.ManagerSettings.ALLOW_UNSAFE_REGISTRATION, true);
            commandManager.setSetting(CommandManager.ManagerSettings.OVERRIDE_EXISTING_COMMANDS, true);

            final Function<ParserParameters, CommandMeta> commandMetaFunction =
                    p -> CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No Description")).build();

            new MinecraftExceptionHandler<CommandSender>()
                    .withInvalidSenderHandler()
                    .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, NO_PERMISSION_FUNCTION)
                    .withArgumentParsingHandler()
                    .withCommandExecutionHandler()
                    .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, INVALID_SYNTAX_FUNCTION)
                    .withDecorator(
                            component -> MiniMessage.miniMessage().parse("<dark_gray>[<#42aaf5>P<#c1d6e6>C<dark_gray>] ").append(component)
                    ).apply(getCommandManager(), p -> p);

            this.annotationParser = new AnnotationParser<>(commandManager, CommandSender.class, commandMetaFunction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SafeVarargs
    public final void registerCommands(Class<? extends SpicyCommand>... commands) {
        for (Class<? extends SpicyCommand> clazz : commands) {
            logger.info("Registering command: " + clazz.getSimpleName());
            try {
                annotationParser.parse(clazz.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerHelp(String commandRoot, String... aliases) {
        logger.info("Registering help for command: " + commandRoot);

        MinecraftHelp<CommandSender> help = new MinecraftHelp<>("/" + commandRoot + " help", p -> p, commandManager);

        help.setHelpColors(MinecraftHelp.HelpColors.of(
                TextColor.color(240, 81, 226), // Primary
                TextColor.color(9, 147, 232), // Highlight
                TextColor.color(86, 198, 232), //alternateHighlight
                TextColor.color(142, 195, 207),
                TextColor.color(73, 252, 255) // accent
        ));
        help.setMaxResultsPerPage(15);

        commandManager.command(getCommandManager().commandBuilder(commandRoot, aliases)
                .literal("help").argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                .handler(context -> {
                    String query = context.getOrDefault("query", "");
                    if (query == null) query = "";
                    help.queryCommands(query, context.getSender());
                }).build());
    }

    public PaperCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }
}
