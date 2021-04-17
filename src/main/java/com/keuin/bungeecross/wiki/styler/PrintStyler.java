package com.keuin.bungeecross.wiki.styler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public interface PrintStyler {
    PrintStyler DEFAULT_STYLER = new PrintStyler() {
        @Override
        public void h1(@NotNull Consumer<BaseComponent[]> consumer, @NotNull String string) {
            Objects.requireNonNull(consumer);
            Objects.requireNonNull(string);
            consumer.accept(new ComponentBuilder("# " + string)
                    .color(ChatColor.GOLD).bold(true).create());
        }

        @Override
        public void link(@NotNull Consumer<BaseComponent[]> consumer,
                         @NotNull String link, String text) {
            Objects.requireNonNull(consumer);
            Objects.requireNonNull(link);
            Objects.requireNonNull(text);
            if (link.equals(text))
                consumer.accept(new ComponentBuilder(String.format("[](%s)", link))
                        .color(ChatColor.BLUE).italic(true).create());
            else
                consumer.accept(new ComponentBuilder(String.format("[%s](%s)", text, link))
                        .color(ChatColor.BLUE).italic(true).create());
        }
    };

    void h1(@NotNull Consumer<BaseComponent[]> consumer, @NotNull String string);

    void link(@NotNull Consumer<BaseComponent[]> consumer, @NotNull String link, String text);
}
