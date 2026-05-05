package net.survivalboom.sbds.api.messages.parsers;

import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractTextParser<self extends AbstractTextParser<self, builder>, builder extends AbstractTextParser.Builder<self, builder>> {

    protected final List<StringParser> parsers = new ArrayList<>();

    protected final Placeholders placeholders = new Placeholders();


    public AbstractTextParser(
            @Nullable Collection<StringParser> parsers,
            @Nullable Placeholders placeholders
    ) {

        if (parsers != null) {
            this.parsers.addAll(parsers);
        }

        if (placeholders != null) {
            this.placeholders.addAll(placeholders);
        }

    }

    // getters //

    public @NotNull Placeholders getPlaceholders() {
        return this.placeholders.copy();
    }

    public @NotNull List<StringParser> getParsers() {
        return new ArrayList<>(parsers);
    }

    // copy //

    public abstract @NotNull builder copy();


    //
    // BUILDER
    //

    public abstract static class Builder<out extends AbstractTextParser<out, self>, self extends Builder<out, self>> {

        protected final List<StringParser> parsers = new ArrayList<>();

        protected final Placeholders placeholders = new Placeholders();


        protected Builder() {}

        protected Builder(@NotNull self builder) {
            this.parsers.addAll(builder.parsers);
            this.placeholders.addAll(builder.placeholders);
        }

        protected Builder(@NotNull out parser) {
            this.parsers.addAll(parser.parsers);
            this.placeholders.addAll(parser.placeholders);
        }

        // MERGE //

        public @NotNull self merge(@NotNull Builder<?, ?> builder) {
            Objects.requireNonNull(builder, "builder == null");
            this.parsers.addAll(builder.parsers);
            this.placeholders.addAll(builder.placeholders);
            return This();
        }

        public @NotNull self merge(@NotNull AbstractTextParser<?, ?> parser) {
            Objects.requireNonNull(parser, "parser == null");
            this.parsers.addAll(parser.parsers);
            this.placeholders.addAll(parser.placeholders);
            return This();
        }

        // PLACEHOLDERS //

        public @NotNull self addPlaceholder(@NotNull String key, @Nullable Object value) {
            this.placeholders.add(key, value);
            return This();
        }

        public @NotNull self addPlaceholder(@NotNull String key, @Nullable Supplier<?> supplier) {
            this.placeholders.add(key, supplier);
            return This();
        }

        public @NotNull self addPlaceholders(@NotNull Object... args) {
            this.placeholders.addAll(Placeholders.of(args));
            return This();
        }

        public @NotNull self addPlaceholders(@Nullable Placeholders placeholders) {

            if (placeholders != null) {
                this.placeholders.addAll(placeholders);
            }

            return This();

        }

        public @NotNull Placeholders getPlaceholders() {
            return this.placeholders.copy();
        }

        // PARSERS //

        public @NotNull self addParser(@NotNull StringParser parser) {
            Objects.requireNonNull(parser, "parser == null");
            this.parsers.add(parser);
            return This();
        }

        public @NotNull self addParsers(@Nullable Collection<StringParser> parsers) {

            this.parsers.clear();

            if (parsers != null) {
                this.parsers.addAll(parsers);
            }

            return This();

        }

        public @NotNull List<StringParser> getParsers() {
            return new ArrayList<>(parsers);
        }

        // BUILD //

        public abstract @NotNull out build();

        public abstract @NotNull self copy();


        @SuppressWarnings("unchecked")
        protected @NotNull self This() {
            return (self) this;
        }

    }

}
