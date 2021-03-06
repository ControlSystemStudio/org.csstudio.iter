<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<databrowser>
    <title>Temperature     </title>
    <save_changes>true</save_changes>
    <show_legend>true</show_legend>
    <show_toolbar>true</show_toolbar>
    <grid>true</grid>
    <scroll>true</scroll>
    <update_period>3.0</update_period>
    <scroll_step>5</scroll_step>
    <start>-1 minutes 0.0 seconds</start>
    <end>now</end>
    <archive_rescale>NONE</archive_rescale>
    <background>
        <red>235</red>
        <green>235</green>
        <blue>235</blue>
    </background>
    <title_font>DejaVu Sans Mono|39|0</title_font>
    <label_font>DejaVu Sans Mono|26|0</label_font>
    <scale_font>DejaVu Sans Mono|20|0</scale_font>
    <legend_font>DejaVu Sans Mono|20|0</legend_font>
    <axes>
        <axis>
            <visible>true</visible>
            <name>CTRL-SUP-CSS:TEMP</name>
            <use_axis_name>true</use_axis_name>
            <use_trace_names>true</use_trace_names>
            <right>false</right>
            <color>
                <red>21</red>
                <green>21</green>
                <blue>196</blue>
            </color>
            <min>-12.0</min>
            <max>12.0</max>
            <grid>true</grid>
            <autoscale>false</autoscale>
            <log_scale>false</log_scale>
        </axis>
        <axis>
            <visible>false</visible>
            <name>sim://noise</name>
            <use_axis_name>true</use_axis_name>
            <use_trace_names>true</use_trace_names>
            <right>false</right>
            <color>
                <red>242</red>
                <green>26</green>
                <blue>26</blue>
            </color>
            <min>-8.475768610132976</min>
            <max>8.871307905961284</max>
            <grid>false</grid>
            <autoscale>true</autoscale>
            <log_scale>false</log_scale>
        </axis>
        <axis>
            <visible>false</visible>
            <name>CTRL-SUP-CSS:TEMP.HIGH</name>
            <use_axis_name>true</use_axis_name>
            <use_trace_names>true</use_trace_names>
            <right>false</right>
            <color>
                <red>33</red>
                <green>179</green>
                <blue>33</blue>
            </color>
            <min>8.0</min>
            <max>9.0</max>
            <grid>false</grid>
            <autoscale>true</autoscale>
            <log_scale>false</log_scale>
        </axis>
    </axes>
    <annotations>
    </annotations>
    <pvlist>
        <pv>
            <display_name>CTRL-SUP-CSS:TEMP</display_name>
            <visible>true</visible>
            <name>CTRL-SUP-CSS:TEMP</name>
            <axis>0</axis>
            <color>
                <red>21</red>
                <green>21</green>
                <blue>196</blue>
            </color>
            <trace_type>LINES</trace_type>
            <linewidth>2</linewidth>
            <point_type>NONE</point_type>
            <point_size>2</point_size>
            <waveform_index>0</waveform_index>
            <period>0.0</period>
            <ring_size>5000</ring_size>
            <request>OPTIMIZED</request>
        </pv>
        <pv>
            <display_name>CTRL-SUP-CSS:TEMP.LOW</display_name>
            <visible>true</visible>
            <name>CTRL-SUP-CSS:TEMP.HOPR</name>
            <axis>0</axis>
            <color>
                <red>242</red>
                <green>26</green>
                <blue>26</blue>
            </color>
            <trace_type>AREA</trace_type>
            <linewidth>2</linewidth>
            <point_type>NONE</point_type>
            <point_size>2</point_size>
            <waveform_index>0</waveform_index>
            <period>0.0</period>
            <ring_size>5000</ring_size>
            <request>OPTIMIZED</request>
        </pv>
        <pv>
            <display_name>CTRL-SUP-CSS:TEMP.HIGH</display_name>
            <visible>true</visible>
            <name>CTRL-SUP-CSS:TEMP.LOPR</name>
            <axis>0</axis>
            <color>
                <red>33</red>
                <green>179</green>
                <blue>33</blue>
            </color>
            <trace_type>AREA</trace_type>
            <linewidth>2</linewidth>
            <point_type>NONE</point_type>
            <point_size>2</point_size>
            <waveform_index>0</waveform_index>
            <period>0.0</period>
            <ring_size>5000</ring_size>
            <request>OPTIMIZED</request>
        </pv>
        <pv>
            <display_name>sim://noise</display_name>
            <visible>true</visible>
            <name>sim://noise(-10,+10,1)</name>
            <axis>0</axis>
            <color>
                <red>255</red>
                <green>165</green>
                <blue>0</blue>
            </color>
            <trace_type>AREA</trace_type>
            <linewidth>2</linewidth>
            <point_type>NONE</point_type>
            <point_size>2</point_size>
            <waveform_index>0</waveform_index>
            <period>0.0</period>
            <ring_size>5000</ring_size>
            <request>OPTIMIZED</request>
        </pv>
    </pvlist>
</databrowser>