package dev.lumas.shops.components;

import com.google.common.base.Preconditions;
import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.config.TranslatorService;
import dev.lumas.shops.constants.PurchaseResult;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;
import dev.lumas.shops.util.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@NullMarked
@AllArgsConstructor
@Accessors(fluent = true)
public class MarketItem implements Keyed {

    private transient final Key key;
    private final Stock stock;
    private final Currency<? extends Number> currency;
    private final Product product;
    private final ItemStack stack;

    public PurchaseResult purchase(Market market, Player player) {
        return purchase(market, player, 1);
    }

    public PurchaseResult purchase(Market market, Player player, int amount) {
        if (!currency.hasEnough(player, amount)) {
            return PurchaseResult.NOT_ENOUGH_CURRENCY;
        }
        PurchaseReceipt fingerPrint = PurchaseReceipt.of(player.getUniqueId(), key);

        if (stock.hasPlayerStock() && market.getPurchasesOf(fingerPrint) + amount > stock.player()) {
            return PurchaseResult.TOO_MANY_PURCHASES;
        } else if (stock.hasGlobalStock() && market.getGlobalPurchasesOf(key) + amount > stock.global()) {
            return PurchaseResult.NOT_ENOUGH_STOCK;
        }

        if (currency.withdraw(player, amount)) {
            market.addPurchase(fingerPrint);
            market.refreshItem(this);
            product.give(player, this, amount);
        } else {
            throw new IllegalStateException("Currency withdraw failed");
        }
        return PurchaseResult.SUCCESS;
    }

    public ItemStack display(MarketState marketState, Locale locale) {
        // We have to rebuild the lore every time because stock may have changed.
        ItemStack stackCopy = stack.clone();
        List<Component> lore = stack.lore();
        List<Component> components = lore != null ? lore : new ArrayList<>();

        addLines(components, locale, "shops.gui.itemstack.description");
        addLines(components, locale, "shops.gui.itemstack.price", currency.readablePrice());
        if (stock.hasGlobalStock()) {
            int globalStock = stock.global();
            addLines(components, locale, "shops.gui.itemstack.stock", marketState.getRemainingGlobalStock(globalStock, key), globalStock);
        }

        stackCopy.lore(components);
        return stackCopy;
    }

    public Component displayName() {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomName()) {
            return Component.text(ClassUtil.formatEnum(stack.getType()));
        }
        return Preconditions.checkNotNull(meta.customName(), "Item meta has no display name");
    }

    private void addLines(List<Component> target, Locale locale, String key, Object... args) {
        String raw = TranslatorService.instance().getMiniMessageString(key, locale);
        if (raw == null) {
            target.add(translate(locale, key, args));
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();

        for (String segment : raw.split("\\\\n|\n", -1)) {
            if (segment.isEmpty()) {
                target.add(Component.empty());
                continue;
            }

            Component component = mm.deserialize(segment);
            for (int i = 0; i < args.length; i++) {
                component = substituteArg(component, i, args[i]);
            }
            target.add(component.decoration(TextDecoration.ITALIC, false));
        }
    }

    private static Component substituteArg(Component source, int index, Object arg) {
        String token = "<arg:" + index + ">";
        Pattern pattern = Pattern.compile(Pattern.quote(token));

        if (arg instanceof ComponentLike c) {
            Component replacement = c.asComponent();
            return source.replaceText(builder -> builder.match(pattern).replacement(replacement));
        }
        String replacement = String.valueOf(arg);
        return source.replaceText(builder -> builder.match(pattern).replacement(replacement));
    }

    protected final Component translate(Locale locale, String key, Object... args) {
        ComponentLike[] argsAsComponents = new ComponentLike[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof ComponentLike) {
                argsAsComponents[i] = (ComponentLike) arg;
            } else {
                argsAsComponents[i] = Component.text(args[i].toString());
            }
        }
        return GlobalTranslator.render(Component.translatable(key, argsAsComponents), locale).decoration(TextDecoration.ITALIC, false);
    }

}
