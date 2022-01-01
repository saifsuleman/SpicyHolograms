package io.pulsarlabs.spicyholograms.command.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import io.pulsarlabs.spicyholograms.SpicyHolograms;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class HologramParser<C> implements ArgumentParser<C, Hologram> {
    @Override
    public @NonNull ArgumentParseResult<Hologram> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
        String input = inputQueue.peek();
        Hologram hologram = SpicyHolograms.getInstance().getHologramsManager().getHologram(input);

        if (input == null || hologram == null) {
            return ArgumentParseResult.failure(new HologramParseException(input == null ? "" : input, commandContext));
        }

        inputQueue.remove();
        return ArgumentParseResult.success(hologram);
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
        return new ArrayList<>(SpicyHolograms.getInstance().getHologramsManager().getHolograms().keySet());
    }

    public static final class HologramParseException extends ParserException {
        private final String input;

        public HologramParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
            super(
                    Hologram.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String getInput() {
            return this.input;
        }
    }
}
