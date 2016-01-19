<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<databrowser>
    <title>Just a noise</title>
    <save_changes>true</save_changes>
    <show_legend>true</show_legend>
    <show_toolbar>true</show_toolbar>
    <grid>true</grid>
    <scroll>true</scroll>
    <update_period>1.0</update_period>
    <scroll_step>5</scroll_step>
    <start>-60.0 seconds</start>
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
            <name>sim://noise</name>
            <use_axis_name>false</use_axis_name>
            <use_trace_names>true</use_trace_names>
            <right>false</right>
            <color>
                <red>21</red>
                <green>21</green>
                <blue>196</blue>
            </color>
            <label_font>DejaVu Sans Mono|26|0</label_font>
            <scale_font>DejaVu Sans Mono|26|0</scale_font>
            <min>-4.95</min>
            <max>4.74</max>
            <grid>true</grid>
            <autoscale>true</autoscale>
            <log_scale>false</log_scale>
        </axis>
    </axes>
    <annotations>
    </annotations>
    <pvlist>
        <pv>
            <display_name>sim://noise</display_name>
            <visible>true</visible>
            <name>sim://noise</name>
            <axis>0</axis>
            <color>
                <red>165</red>
                <green>42</green>
                <blue>42</blue>
            </color>
            <trace_type>AREA</trace_type>
            <linewidth>2</linewidth>
            <point_type>NONE</point_type>
            <point_size>2</point_size>
            <waveform_index>0</waveform_index>
            <period>0.0</period>
            <ring_size>65</ring_size>
            <request>OPTIMIZED</request>
            <archive>
                <name>Archive RDB</name>
                <url>jdbc:postgresql://localhost/css_archive_3_0_0</url>
                <key>1</key>
            </archive>
        </pv>
    </pvlist>
</databrowser>