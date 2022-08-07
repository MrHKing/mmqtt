package org.monkey.mmq.plugs;

import org.monkey.mmq.plugs.view.MmqPlugsView;

public interface IMmqPlugs {

    MmqPlugsView getMmqPlugsView();

    String getPlugsCode();

    void update(MmqPlugsView mmqPlugsView);

    void setLocalMqttPort(int port);
}
