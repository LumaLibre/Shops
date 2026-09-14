package dev.lumas.shops.components.requirement;

public class MistralRequirement implements PermissionRequirement {
    @Override
    public String permission() {
        return "group.mistral";
    }

    @Override
    public String extraText() {
        return "<white>★ <#CEFACF>Rank<gray>:<b><gradient:#fff2be:#fbaeb4:#FBABFD>Mistral Rank</gradient></b>";
    }
}
