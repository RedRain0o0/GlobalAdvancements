package io.github.redrain0o0.globaladvancements.client.screens;

import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wily.factoryapi.FactoryAPI;
import wily.factoryapi.base.Stocker;
import wily.factoryapi.util.PagedList;
import wily.legacy.client.screen.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import static wily.legacy.client.screen.LegacyAdvancementsScreen.getActualAdvancements;


public class AdvancementsScreen extends PanelVListScreen implements TabList.Access {

    public static final Component TITLE = Component.translatable("gui.advancements");
    protected final Stocker.Sizeable page = new Stocker.Sizeable(0);
    protected final TabList tabList = new TabList(new PagedList<>(page,10));
    protected final List<DisplayInfo> displayInfos = new ArrayList<>();
    protected boolean showDescription = false;
    protected boolean oldLegacyTooltipsValue;
    public static final List<ResourceLocation> vanillaOrder = List.of(FactoryAPI.createVanillaLocation("story/root"),FactoryAPI.createVanillaLocation("adventure/root"), FactoryAPI.createVanillaLocation("husbandry/root"),FactoryAPI.createVanillaLocation("nether/root"),FactoryAPI.createVanillaLocation("end/root"));

    public AdvancementsScreen(Screen parent) {
        super(parent,s-> Panel.createPanel(s, p-> p.centeredLeftPos(s), p-> p.centeredTopPos(s) + (((AdvancementsScreen)s).displayInfos.isEmpty() ? 0 : 18), 450,252),TITLE);
        renderableVLists.clear();
        /*StreamSupport.stream(getActualAdvancements().roots().spliterator(),false).sorted(Comparator.comparingInt(n->vanillaOrder.contains(n.holder().id()) ? vanillaOrder.indexOf(n.holder().id()): Integer.MAX_VALUE)).forEach(a-> {
            DisplayInfo displayInfo = a.advancement().display().orElse(null);
            if (displayInfo == null) return;

            tabList.addTabButton(43, 0, LegacyTabButton.iconOf(displayInfo.getIcon()), displayInfo.getTitle(), b -> repositionElements());
            RenderableVList renderableVList = new RenderableVList(this).layoutSpacing(l -> 4).forceWidth(false).cyclic(false);
            renderableVLists.add(renderableVList);
            displayInfos.add(displayInfo);
            getActualAdvancements().nodes().stream().filter(n1 -> !n1.equals(a) && n1.root().equals(a)).sorted(Comparator.comparingInt(LegacyAdvancementsScreen::getRootDistance)).forEach(node -> addAdvancementButton(renderableVList, node));
        });*/
    }

    protected void addAdvancementButton(RenderableVList renderableVList, AdvancementNode advancementNode){
        advancementNode.advancement().display().ifPresent(info-> renderableVList.addRenderable(new LegacyAdvancementsScreen.AdvancementButton(0,0,38,38,advancementNode,info)));
    }

    @Override
    public TabList getTabList() {
        return tabList;
    }

    @Override
    public RenderableVList getRenderableVList() {
        return getRenderableVLists().get(tabList.selectedTab);
    }
}