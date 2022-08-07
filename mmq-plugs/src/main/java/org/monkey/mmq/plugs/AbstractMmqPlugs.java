package org.monkey.mmq.plugs;

import org.monkey.mmq.plugs.view.MmqPlugsView;
import org.monkey.mmq.plugs.view.PropertyItem;

import java.util.Optional;

/**
 * @ClassName:AbstractMmqPlugs
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/7 20:15
 * @Version: v1.0
 */

public abstract class AbstractMmqPlugs implements IMmqPlugs {
    protected MmqPlugsView mmqPlugsView;

    protected int port;

    protected abstract boolean stop();

    protected abstract boolean start();


    protected abstract void init(MmqPlugsView mmqPlugsView);

    protected AbstractMmqPlugs() {
        mmqPlugsView = new MmqPlugsView();
        this.init(mmqPlugsView);
        mmqPlugsView.setEnable(false);
    }

    public void enable() {
        this.stop();
        if (this.mmqPlugsView.isEnable()) {
            this.start();
        }
        //this.mmqPlugsView.setEnable(true);
    }

    @Override
    public void update(MmqPlugsView mmqPlugsView) {
        this.mmqPlugsView = mmqPlugsView;
        this.enable();
    }

    @Override
    public void setLocalMqttPort(int port) {
        this.port = port;
    }

    public String getPlugsCode() {
        if (mmqPlugsView.getPlugsCode() == null || mmqPlugsView.getPlugsCode() == "") {
            return null;
        } else {
            return mmqPlugsView.getPlugsCode();
        }
    }

    @Override
    public MmqPlugsView getMmqPlugsView() {
        return mmqPlugsView;
    }

    protected Optional<PropertyItem> getValueByCode(String code) {
        return this.mmqPlugsView.getPropertyItems().stream().filter(x->x.getCode().equals(code)).findFirst();
    }
}
