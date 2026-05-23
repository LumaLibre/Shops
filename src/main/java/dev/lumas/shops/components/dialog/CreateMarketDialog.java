package dev.lumas.shops.components.dialog;

import dev.lumas.shops.components.MarketCreator;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.util.Numbers;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Setter
@NullMarked
@Accessors(fluent = true)
@SuppressWarnings("UnstableApiUsage")
public class CreateMarketDialog extends ShopsDialog {

    private static final String INPUT_NAME = "name";
    private static final String INPUT_SIZE = "size";
    private static final String INPUT_SLOTS = "slots";

    private final KeyConsumer<CreateMarketDialog> submit = KeyConsumer.of(
            this,
            Key.key("shops:create/submit"),
            CreateMarketDialog::onConfirm
    );

    private final Key marketKey;
    private @Nullable MarketTemplate template;

    public CreateMarketDialog(Locale locale, Key marketKey) {
        super(locale);
        this.marketKey = marketKey;
    }

    @Override
    public Dialog build() {
        DialogInput nameInput = DialogInput.text(INPUT_NAME, translate("shops.create.input.name"))
                .maxLength(Integer.MAX_VALUE)
                .width(300)
                .initial(orEmpty(template == null ? null : template.title()))
                .build();

        DialogInput sizeInput = DialogInput.numberRange(INPUT_SIZE, translate("shops.create.input.size"), 9f, 54f)
                .step(9f)
                .initial(template == null ? 27f : template.size())
                .labelFormat("%s: %s slots")
                .width(300)
                .build();

        // e.g. "10-16, 19, 22-24" flattened to [10,11,12,13,14,15,16,19,22,23,24]
        DialogInput slotsInput = DialogInput.text(INPUT_SLOTS, translate("shops.create.input.slots"))
                .maxLength(256)
                .initial(template == null ? "10-16" : serializeSlotRanges(template.contentSlots().slots()))
                .width(300)
                .build();

        ActionButton submitButton = ActionButton.builder(translate("shops.create.button.submit"))
                .action(DialogAction.customClick(submit.key(), null))
                .build();

        ActionButton cancelButton = ActionButton.builder(translate("shops.create.button.cancel"))
                .action(null)
                .build();

        DialogBase base = DialogBase.builder(translate("shops.create.title"))
                .canCloseWithEscape(true)
                .body(List.of(
                        DialogBody.plainMessage(translate("shops.create.description"), 300)
                ))
                .inputs(List.of(nameInput, sizeInput, slotsInput))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(submitButton, cancelButton))
        );
    }

    private void onConfirm(Player player, DialogResponseView view) {
        String rawTitle = view.getText(INPUT_NAME);
        int size = Math.round(Numbers.unbox(view.getFloat(INPUT_SIZE), 54f));
        String rawSlots = view.getText(INPUT_SLOTS);

        if (rawTitle == null || rawTitle.isBlank()) {
            Viewers.sendMessage(player, "shops.create.error.empty_name");
            return;
        }

        List<Integer> contentSlots;
        try {
            contentSlots = parseSlotRanges(rawSlots, size);
        } catch (NumberFormatException e) {
            Viewers.sendMessage(player, "shops.create.error.invalid_slots");
            return;
        }

        if (contentSlots.isEmpty()) {
            Viewers.sendMessage(player, "shops.create.error.no_slots");
            return;
        }

        Component title = MiniMessage.miniMessage().deserialize(rawTitle);
        boolean isNewMarket = isNewMarket();

        if (isNewMarket && MarketManager.INSTANCE.exists(marketKey)) {
            Viewers.sendMessage(player, "shops.create.error.exists");
            return;
        }

        MarketCreator marketCreator = new MarketCreator(marketKey, title, size, SlotList.of(contentSlots), template);
        marketCreator.open(player);
    }

    public boolean isNewMarket() {
        return template == null;
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, submit);
        player.showDialog(build());
    }

    private static String orEmpty(@Nullable Component component) {
        return component == null ? "" : MiniMessage.miniMessage().serialize(component);
    }

    /**
     * Parses a slot-range string into a list of slot indices.
     * Accepts comma-separated entries; each is either a single int ("5") or a range ("10-16").
     * Indices outside [0, size) are filtered out.
     */
    private static List<Integer> parseSlotRanges(@Nullable String raw, int size) {
        List<Integer> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) return result;

        for (String part : raw.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;

            int dash = part.indexOf('-');
            if (dash < 0) {
                int slot = Integer.parseInt(part);
                if (slot >= 0 && slot < size) result.add(slot);
            } else {
                int from = Integer.parseInt(part.substring(0, dash).trim());
                int to = Integer.parseInt(part.substring(dash + 1).trim());
                int lo = Math.min(from, to);
                int hi = Math.max(from, to);
                for (int i = lo; i <= hi; i++) {
                    if (i >= 0 && i < size) result.add(i);
                }
            }
        }
        return result;
    }

    /**
     * Serializes a list of slot indices into the same comma-separated range string
     * that {@link #parseSlotRanges} accepts. Consecutive runs collapse to {@code lo-hi};
     * isolated indices stay as single numbers.
     *
     * Examples:
     *   [10,11,12,13,14,15,16,19,22,23,24] → "10-16, 19, 22-24"
     *   [5]                                → "5"
     *   []                                 → ""
     */
    private static String serializeSlotRanges(@Nullable List<Integer> slots) {
        if (slots == null || slots.isEmpty()) return "";

        List<Integer> sorted = new ArrayList<>(slots);
        Collections.sort(sorted);

        StringBuilder sb = new StringBuilder();
        int runStart = sorted.get(0);
        int runEnd = runStart;

        for (int i = 1; i < sorted.size(); i++) {
            int n = sorted.get(i);
            if (n == runEnd + 1) {
                runEnd = n;
            } else if (n == runEnd) {
                // do nothing
            } else {
                appendRun(sb, runStart, runEnd);
                runStart = n;
                runEnd = n;
            }
        }
        appendRun(sb, runStart, runEnd);
        return sb.toString();
    }

    private static void appendRun(StringBuilder sb, int lo, int hi) {
        if (!sb.isEmpty()) sb.append(", ");
        if (lo == hi) sb.append(lo);
        else sb.append(lo).append('-').append(hi);
    }
}