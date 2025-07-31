package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.Inspector;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.profiler.RenderProfilerCounter;
import foundry.veil.impl.client.render.profiler.VeilRenderProfilerImpl;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.*;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiStatus.Internal
public class PipelineStatisticsViewer implements Inspector {

    public static final Component TITLE = Component.translatable("inspector.veil.pipeline_statistics.title");
    public static final int HISTORY_LENGTH = 200;

    private final ImBoolean open = new ImBoolean();
    private final Set<String> enabledPaths = new HashSet<>();
    private final ObjectList<Map<String, VeilRenderProfilerImpl.ResultField>> history = new ObjectArrayList<>(HISTORY_LENGTH);
    private int historyIndex = 0;

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public Component getGroup() {
        return INFO_GROUP;
    }

    @Override
    public boolean isEnabled() {
        return VeilRenderSystem.pipelineStatisticsQuerySupported();
    }

    private void renderComponents() {
        if (!VeilRenderSystem.pipelineStatisticsQuerySupported()) {
            return;
        }

        while (this.history.size() < HISTORY_LENGTH) {
            this.history.add(new Object2ObjectOpenHashMap<>());
        }

        VeilRenderProfilerImpl.setEnabled(this.enabledPaths, RenderProfilerCounter.ALL);

        this.enabledPaths.clear();
        this.renderCounters("", RenderProfilerCounter.FRAGMENT_SHADER_INVOCATIONS);
        this.historyIndex++;
        this.historyIndex %= HISTORY_LENGTH;
        this.getHistory(0).clear();
    }

    private Map<String, VeilRenderProfilerImpl.ResultField> getHistory(int past) {
        return this.history.get(Math.floorMod(this.historyIndex - past, HISTORY_LENGTH));
    }

    private void renderCounters(String path, RenderProfilerCounter sortedStatistic) {
        List<VeilRenderProfilerImpl.ResultField> fields = VeilRenderProfilerImpl.getCounters(path, sortedStatistic);
        Map<String, VeilRenderProfilerImpl.ResultField> map = this.history.get(this.historyIndex);

        for (VeilRenderProfilerImpl.ResultField field : fields) {
            String label = path.isBlank() ? field.name() : (path.replaceAll("\u001e", ".") + "." + field.name());
            if (ImGui.collapsingHeader(field.name())) {
                ImGui.pushID(label);
                ImGui.indent();
                map.put(label, field);

                if (ImGui.collapsingHeader("Plot")) {
                    this.enabledPaths.add(path.isBlank() ? field.name() : path + '\u001e' + field.name());
                    RenderProfilerCounter[] statistics = field.statistics();
                    long[] values = new long[HISTORY_LENGTH];
                    for (RenderProfilerCounter statistic : statistics) {
                        if (ImPlot.beginPlot(statistic.name(), ImGui.getContentRegionAvailX(), 150, ImPlotFlags.NoFrame | ImPlotFlags.NoChild | ImPlotFlags.NoMouseText | ImPlotFlags.NoLegend)) {
                            long max = 0;
                            for (int j = 0; j < HISTORY_LENGTH; j++) {
                                VeilRenderProfilerImpl.ResultField past = this.getHistory(HISTORY_LENGTH - j - 1).get(label);
                                if (past != null) {
                                    long value = past.count()[statistic.ordinal()];
                                    values[j] = value;
                                    if (value > max) {
                                        max = value;
                                    }
                                }
                            }

                            ImPlot.setupAxis(ImPlotAxis.X1, ImPlotAxisFlags.NoTickMarks | ImPlotAxisFlags.NoTickLabels);
                            ImPlot.setupAxis(ImPlotAxis.Y1, ImPlotAxisFlags.NoTickMarks);
                            ImPlot.setupAxesLimits(0, HISTORY_LENGTH, 0, Math.max(max * 1.1, 10), ImPlotCond.Always);
                            ImPlot.setupAxisFormat(ImPlotAxis.Y1, "%3.0f");

                            ImPlot.pushStyleColor(ImPlotCol.Line, VeilImGuiUtil.colorOf(statistic.name()));
                            ImPlot.pushStyleVar(ImPlotStyleVar.PlotPadding, 0.0F, 0.0F);
                            ImPlot.pushStyleVar(ImPlotStyleVar.LabelPadding, 0.0F, 0.0F);
                            ImPlot.plotLine(statistic.name(), values);
                            ImPlot.popStyleVar();
                            ImPlot.popStyleColor();

                            ImPlot.endPlot();
                        }
                    }
                    ImGui.newLine();
                }

                this.renderCounters(label, sortedStatistic);
                ImGui.unindent();
                ImGui.popID();
            }
        }
    }

    @Override
    public void render() {
    }

    @Override
    public void renderLast() {
        ImGui.setNextWindowSizeConstraints(400, 460, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin(this.getDisplayName().getString(), this.open)) {
            this.renderComponents();
        }
        ImGui.end();

        if (!this.open.get()) {
            VeilRenderSystem.renderer().getEditorManager().hide(this);
        }
    }

    @Override
    public void onShow() {
        this.open.set(true);
    }

    @Override
    public void onHide() {
        Inspector.super.onHide();
        this.history.clear();
        this.historyIndex = 0;
    }
}
