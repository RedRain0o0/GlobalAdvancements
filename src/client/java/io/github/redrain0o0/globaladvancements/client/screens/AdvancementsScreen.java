package io.github.redrain0o0.globaladvancements.client.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wily.factoryapi.base.client.FactoryGuiGraphics;
import wily.factoryapi.base.Stocker;
import wily.factoryapi.util.PagedList;
import wily.legacy.client.CommonColor;
import wily.legacy.client.ControlType;
import wily.legacy.client.controller.ControllerBinding;
import wily.legacy.client.screen.*;
import wily.legacy.util.LegacyComponents;
import wily.legacy.util.LegacySprites;
import wily.legacy.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import static wily.legacy.client.screen.ControlTooltip.COMPOUND_ICON_FUNCTION;


public class AdvancementsScreen extends PanelVListScreen implements TabList.Access {

    public static final Component TITLE = Component.translatable("gui.advancements");
    protected final Stocker.Sizeable page = new Stocker.Sizeable(0);
    protected final TabList tabList = new TabList(new PagedList<>(page,10));
    protected final List<DisplayInfo> displayInfos = new ArrayList<>();
    protected boolean showDescription = false;
    //protected boolean oldLegacyTooltipsValue;
    //public static final List<ResourceLocation> vanillaOrder = List.of(FactoryAPI.createVanillaLocation("story/root"),FactoryAPI.createVanillaLocation("adventure/root"), FactoryAPI.createVanillaLocation("husbandry/root"),FactoryAPI.createVanillaLocation("nether/root"),FactoryAPI.createVanillaLocation("end/root"));

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

    @Override
    public void renderDefaultBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        ScreenUtil.renderDefaultBackground(accessor, guiGraphics, false);
    }

    /*protected void addAdvancementButton(RenderableVList renderableVList, AdvancementNode advancementNode){
        advancementNode.advancement().display().ifPresent(info-> renderableVList.addRenderable(new LegacyAdvancementsScreen.AdvancementButton(0,0,38,38,advancementNode,info)));
    }*/

    /*@Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        RenderSystem.enableBlend();
    }*/

    @Override
    protected void panelInit() {
        addRenderableWidget(tabList);
        super.panelInit();
        addRenderableOnly(((guiGraphics, i, j, f) ->{
            guiGraphics.drawString(font,showDescription && !tabList.tabButtons.isEmpty() ? tabList.tabButtons.get(tabList.selectedTab).getMessage() : getTitle(),panel.x + (panel.width - font.width(showDescription && !tabList.tabButtons.isEmpty() ? tabList.tabButtons.get(tabList.selectedTab).getMessage() : getTitle()))/ 2,panel.y + 10, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
            ScreenUtil.renderPanelTranslucentRecess(guiGraphics,panel.x + 12, panel.y + 22, 426, 27);
            FactoryGuiGraphics.of(guiGraphics).blitSprite(LegacySprites.PANEL_RECESS,panel.x + 12, panel.y + 50, 426, 186);
        }));
    }

    @Override
    public TabList getTabList() {
        return tabList;
    }

    @Override
    public RenderableVList getRenderableVList() {
        return getRenderableVLists().get(tabList.selectedTab);
    }

    /*@Override
    protected void panelInit() {
        addRenderableWidget(tabList);
        super.panelInit();
        addRenderableOnly(((guiGraphics, i, j, f) ->{
            guiGraphics.drawString(font,showDescription && !tabList.tabButtons.isEmpty() ? tabList.tabButtons.get(tabList.selectedTab).getMessage() : getTitle(),panel.x + (panel.width - font.width(showDescription && !tabList.tabButtons.isEmpty() ? tabList.tabButtons.get(tabList.selectedTab).getMessage() : getTitle()))/ 2,panel.y + 10, CommonColor.INVENTORY_GRAY_TEXT.get(),false);
            if (!displayInfos.isEmpty()) {
                ResourceLocation background = displayInfos.get(tabList.selectedTab).getBackground().orElse(null);
                if (background != null) FactoryGuiGraphics.of(guiGraphics).blit(background,panel.x + 14, panel.y + 24,0,0,422,23,16,16);
            }
            ScreenUtil.renderPanelTranslucentRecess(guiGraphics,panel.x + 12, panel.y + 22, 426, 27);
            if (getFocused() instanceof LegacyAdvancementsScreen.AdvancementButton a) guiGraphics.drawString(font,a.info.getTitle(),panel.x + (panel.width - font.width(a.info.getTitle()))/ 2,panel.y + 32,0xFFFFFF);
            RenderSystem.disableBlend();
            FactoryGuiGraphics.of(guiGraphics).blitSprite(LegacySprites.PANEL_RECESS,panel.x + 12, panel.y + 50, 426, 186);
        }));
        tabList.init(panel.x,panel.y - 37,panel.width,(b,i)->{
            int index = tabList.tabButtons.indexOf(b);
            b.type = index == 0 ? 0 : index >= 9 ? 2 : 1;
            b.setWidth(45);
            b.offset = (t1) -> new Vec3(0, t1.selected ? 0 : 4.5, 0);
        });
    }*/

    @Override
    public void renderableVListInit() {
        getRenderableVList().init(panel.x + 17, panel.y + 55, 416,176);
    }

    //@Override
    //public boolean keyPressed(int i, int j, int k) {
    //    if (i == InputConstants.KEY_X){
    //        showDescription = !showDescription;
    //        return true;
    //    }
    //    if (tabList.controlTab(i)) return true;
    //    if (hasShiftDown()) tabList.controlPage(page,i == 263 , i == 262);
    //    return super.keyPressed(i, j, k);
    //}

    @Override
    public void addControlTooltips(ControlTooltip.Renderer renderer) {
        super.addControlTooltips(renderer);
        renderer.tooltips.remove(0);
        renderer.
                add(ControlTooltip.EXTRA::get,()-> LegacyComponents.SHOW_DESCRIPTION).
                add(()-> page.max > 0 ? ControlType.getActiveType().isKbm() ? COMPOUND_ICON_FUNCTION.apply(new ControlTooltip.Icon[]{ControlTooltip.getKeyIcon(InputConstants.KEY_LSHIFT),ControlTooltip.PLUS_ICON,ControlTooltip.getKeyIcon(InputConstants.KEY_LEFT),ControlTooltip.SPACE_ICON,ControlTooltip.getKeyIcon(InputConstants.KEY_RIGHT)}) : ControllerBinding.RIGHT_STICK.bindingState.getIcon() : null,()->LegacyComponents.PAGE);
    }
}