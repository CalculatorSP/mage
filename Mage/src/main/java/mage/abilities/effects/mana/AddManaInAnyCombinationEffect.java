package mage.abilities.effects.mana;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.common.ManaEffect;
import mage.constants.ColoredManaSymbol;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

/**
 * @author LevelX2
 */
public class AddManaInAnyCombinationEffect extends ManaEffect {

    private ArrayList<ColoredManaSymbol> manaSymbols = new ArrayList<>();
    private final DynamicValue amount;
    private final DynamicValue netAmount;

    public AddManaInAnyCombinationEffect(int amount) {
        this(StaticValue.get(amount), StaticValue.get(amount), ColoredManaSymbol.B, ColoredManaSymbol.U, ColoredManaSymbol.R, ColoredManaSymbol.W, ColoredManaSymbol.G);
    }

    public AddManaInAnyCombinationEffect(int amount, ColoredManaSymbol... coloredManaSymbols) {
        this(StaticValue.get(amount), StaticValue.get(amount), coloredManaSymbols);
    }

    public AddManaInAnyCombinationEffect(DynamicValue amount, DynamicValue netAmount, ColoredManaSymbol... coloredManaSymbols) {
        super();
        this.manaSymbols.addAll(Arrays.asList(coloredManaSymbols));
        this.amount = amount;
        this.staticText = setText();
        this.netAmount = netAmount;
    }

    public AddManaInAnyCombinationEffect(int amount, String text) {
        this(amount);
        this.staticText = text;
    }

    public AddManaInAnyCombinationEffect(int amount, String text, ColoredManaSymbol... coloredManaSymbols) {
        this(amount, coloredManaSymbols);
        this.staticText = text;
    }

    public AddManaInAnyCombinationEffect(DynamicValue amount, DynamicValue netAmount, String text, ColoredManaSymbol... coloredManaSymbols) {
        this(amount, netAmount, coloredManaSymbols);
        this.staticText = text;
    }

    public AddManaInAnyCombinationEffect(final AddManaInAnyCombinationEffect effect) {
        super(effect);
        this.manaSymbols = effect.manaSymbols;
        this.amount = effect.amount;
        if (effect.netAmount != null) {
            this.netAmount = effect.netAmount.copy();
        } else {
            this.netAmount = null;
        }
    }

    @Override
    public AddManaInAnyCombinationEffect copy() {
        return new AddManaInAnyCombinationEffect(this);
    }

    @Override
    public List<Mana> getNetMana(Game game, Ability source) {
        List<Mana> netMana = new ArrayList<>();
        if (game != null) {
            if (game.inCheckPlayableState()) {
                int amountAvailableMana = netAmount.calculate(game, source, this);
                if (amountAvailableMana > 0) {
                    if (manaSymbols.size() == 5) { // Any color
                        netMana.add(new Mana(0, 0, 0, 0, 0, 0, amountAvailableMana, 0));
                    } else {
                        generatePossibleManaCombinations(netMana, manaSymbols, amountAvailableMana);
                    }
                }
            } else {
                int amountOfManaLeft = amount.calculate(game, source, this);
                if (amountOfManaLeft > 0) {
                    netMana.add(Mana.AnyMana(amountOfManaLeft));
                }
            }
        }
        return netMana;
    }

    private void generatePossibleManaCombinations(List<Mana> combinations, ArrayList<ColoredManaSymbol> manaSymbols, int amountAvailableMana) {
        List<Mana> copy = new ArrayList<>();
        for (int i = 0; i < amountAvailableMana; i++) {
            for (ColoredManaSymbol colorSymbol : manaSymbols) {
                if (i == 0) {
                    combinations.add(new Mana(colorSymbol));
                } else {
                    for (Mana prevMana : copy) {
                        Mana newMana = new Mana();
                        newMana.add(prevMana);
                        newMana.add(new Mana(colorSymbol));
                        combinations.add(newMana);
                    }
                }
            }
            if (i + 1 < amountAvailableMana) {
                copy.clear();
                copy.addAll(combinations);
                combinations.clear();
            }
        }
    }

    @Override
    public Mana produceMana(Game game, Ability source
    ) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            Mana mana = new Mana();
            int amountOfManaLeft = amount.calculate(game, source, this);
            int maxAmount = amountOfManaLeft;

            while (amountOfManaLeft > 0 && player.canRespond()) {
                for (ColoredManaSymbol coloredManaSymbol : manaSymbols) {
                    int number = player.getAmount(0, amountOfManaLeft, "Distribute mana by color (done " + mana.count()
                            + " of " + maxAmount + "). How many mana add to <b>" + coloredManaSymbol.getColorHtmlName() + "</b> (enter 0 for pass to next color)?", game);
                    if (number > 0) {
                        for (int i = 0; i < number; i++) {
                            mana.add(new Mana(coloredManaSymbol));
                        }
                        amountOfManaLeft -= number;
                    }
                    if (amountOfManaLeft == 0) {
                        break;
                    }
                }
            }

            return mana;
        }
        return null;
    }

    private String setText() {
        StringBuilder sb = new StringBuilder("Add ");
        sb.append(CardUtil.numberToText(amount.toString()));
        sb.append(" mana in any combination of ");
        if (manaSymbols.size() == 5) {
            sb.append("colors");
        } else {
            int i = 0;
            for (ColoredManaSymbol coloredManaSymbol : manaSymbols) {
                i++;
                if (i > 1) {
                    sb.append(" and/or ");
                }
                sb.append('{').append(coloredManaSymbol.toString()).append('}');
            }
        }
        return sb.toString();
    }
}
