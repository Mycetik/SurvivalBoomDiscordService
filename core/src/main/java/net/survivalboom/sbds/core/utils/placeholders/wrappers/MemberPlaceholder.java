package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Member;

public class MemberPlaceholder implements IPlaceholders {

    private final Member member;

    public MemberPlaceholder(@NotNull Member member) {
        this.member = member;
    }


    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                "id", member.getId(),
                "name", member.getEffectiveName(),
                "user", member.getUser(),
                "guild", member.getGuild(),
                "mention", member.getAsMention()
        );
    }

}
