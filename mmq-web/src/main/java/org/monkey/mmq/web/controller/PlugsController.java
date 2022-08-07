package org.monkey.mmq.web.controller;

import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.core.plugs.PlugsFactory;
import org.monkey.mmq.plugs.view.MmqPlugsView;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:PlugsController
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/7 20:57
 * @Version: v1.0
 */
@RestController
@RequestMapping({"/v1/plugs"})
public class PlugsController {
    /**
     * Get plugs.
     *
     * @return Current plugs
     */
    @GetMapping()
    public Object getModules() {
        List<MmqPlugsView> mmqPlugsViewList = new ArrayList<>();
        PlugsFactory.getPlugsList().values().forEach(x-> {
            mmqPlugsViewList.add(x.getMmqPlugsView());
        });
        return mmqPlugsViewList;
    }

    /**
     * update plugs.
     *
     * @param mmqPlugsView
     * @return ok if create succeed
     */
    @PutMapping
    public Object update(@RequestBody MmqPlugsView mmqPlugsView) {
        if (mmqPlugsView == null) return RestResultUtils.success("mmqPlugsView must nut null", null);
        PlugsFactory.update(mmqPlugsView);
        return RestResultUtils.success("update plugs ok!", null);
    }
}
