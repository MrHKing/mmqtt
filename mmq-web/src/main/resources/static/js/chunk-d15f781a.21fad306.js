(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-d15f781a"],{"042a":function(e,t,s){},"0fea":function(e,t,s){"use strict";s.d(t,"c",(function(){return o})),s.d(t,"d",(function(){return a})),s.d(t,"b",(function(){return n})),s.d(t,"a",(function(){return i}));var r=s("b775");function o(e,t){return Object(r["b"])({url:e,method:"post",data:t})}function a(e,t){return Object(r["b"])({url:e,method:"put",data:t})}function n(e,t){return Object(r["b"])({url:e,method:"get",params:t})}function i(e,t){return Object(r["b"])({url:e,method:"delete",params:t})}},3682:function(e,t,s){"use strict";s.r(t);var r=function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("a-card",{attrs:{bordered:!1}},[s("div",{staticClass:"table-operator"}),s("a-list",{attrs:{grid:{gutter:24,lg:3,md:2,sm:1,xs:1},loading:e.loading,"data-source":e.data},scopedSlots:e._u([{key:"renderItem",fn:function(t){return s("a-list-item",{},[s("a-card",{attrs:{title:t.moduleName}},[s("div",{attrs:{slot:"title"},slot:"title"},[e._v(e._s(t.moduleName))]),s("img",{attrs:{slot:"cover",alt:"example",src:"https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"},slot:"cover"}),s("template",{staticClass:"ant-card-actions",slot:"actions"},[s("a",{attrs:{slot:"actions"},on:{click:function(s){return e.handleUpdate(t)}},slot:"actions"},[e._v("编辑")]),s("a-switch",{attrs:{checked:t.enable},on:{change:function(s){return e.handleCheck(s,t)}}},[s("a-icon",{attrs:{slot:"checkedChildren",type:"check"},slot:"checkedChildren"}),s("a-icon",{attrs:{slot:"unCheckedChildren",type:"close"},slot:"unCheckedChildren"})],1)],1),e._v(" "+e._s(t.description)+" ")],2)],1)}}])}),s("AuthModuleModel",{ref:"AuthModuleModel",on:{ok:e.loadData}})],1)},o=[],a=s("0fea"),n=function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("a-drawer",{attrs:{confirmLoading:e.confirmLoading,title:"保存新资源",width:720,visible:e.visible,"body-style":{paddingBottom:"80px"}},on:{close:e.onClose}},[s("a-form",{attrs:{form:e.form,layout:"vertical","hide-required-mark":""}},[s("a-form-item",{attrs:{label:"资源类型"}},[s("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["configs.resourceType",{rules:[{required:!0,message:"请选择资源类型"}]}],expression:"[\n          'configs.resourceType',\n          {\n            rules: [{ required: true, message: '请选择资源类型' }]\n          }\n        ]"}],attrs:{placeholder:"请选择资源类型"},on:{change:e.typeChange}},[s("a-select-option",{attrs:{value:"MYSQL"}},[e._v(" Mysql ")]),s("a-select-option",{attrs:{value:"POSTGRESQL"}},[e._v(" Postgresql ")]),s("a-select-option",{attrs:{value:"SQLSERVER"}},[e._v(" SqlServer ")])],1)],1),s("a-form-item",{attrs:{label:"资源id"}},[s("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["configs.resourceId",{rules:[{required:!0,message:"请选择资源id"}]}],expression:"[\n          'configs.resourceId',\n          {\n            rules: [{ required: true, message: '请选择资源id' }]\n          }\n        ]"}],attrs:{placeholder:"请选择资源id"}},e._l(e.selectResources,(function(t,r){return s("a-select-option",{key:r,attrs:{value:t.resourceID}},[e._v(" "+e._s(t.resourceID)+" ")])})),1)],1),s("a-form-item",{attrs:{label:"表名"}},[s("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["configs.table",{rules:[{required:!0,message:"请输入表名"}]}],expression:"['configs.table', { rules: [{ required: true, message: '请输入表名' }] }]"}],attrs:{placeholder:"请输入表名"}})],1),s("a-form-item",{attrs:{label:"账户字段"}},[s("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["configs.username",{rules:[{required:!0,message:"请输入账户字段"}]}],expression:"['configs.username', { rules: [{ required: true, message: '请输入账户字段' }] }]"}],attrs:{placeholder:"请输入账户字段"}})],1),s("a-form-item",{attrs:{label:"密码字段"}},[s("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["configs.password",{rules:[{required:!0,message:"请输入密码字段"}]}],expression:"['configs.password', { rules: [{ required: true, message: '请输入密码字段' }] }]"}],attrs:{placeholder:"请输入密码字段"}})],1)],1),s("div",{style:{position:"absolute",right:0,bottom:0,width:"100%",borderTop:"1px solid #e9e9e9",padding:"10px 16px",background:"#fff",textAlign:"right",zIndex:1}},[s("a-button",{style:{marginRight:"8px"},on:{click:e.onClose}},[e._v(" Cancel ")]),s("a-button",{attrs:{type:"primary"},on:{click:e.handleOk}},[e._v(" Submit ")])],1)],1)},i=[],c=(s("159b"),s("a4d3"),s("e01a"),s("4de4"),s("d3b7"),{data:function(){return{title:"操作",visible:!1,curRecord:null,confirmLoading:!1,curResources:[],selectResources:[],type:"",form:this.$form.createForm(this),url:{listResources:"/v1/resources/resources",update:"/v1/modules"}}},created:function(){this.listResources()},methods:{listResources:function(){var e=this;Object(a["b"])(this.url.listResources,{}).then((function(t){e.curResources=[],t.data.forEach((function(t){e.curResources.push(t)}))}))},update:function(e){var t=this;this.form.resetFields(),this.visible=!0,this.curRecord=e,e&&(this.key=e.key,this.$nextTick((function(){t.form.setFieldsValue({key:e.key,enable:e.enable,description:e.description,icon:e.icon,modelEnum:e.modelEnum,configs:{resourceType:e.configs.resourceType,resourceId:e.configs.resourceId,table:e.configs.table,username:e.configs.username,password:e.configs.password}})})))},typeChange:function(e){this.selectResources=this.curResources.filter((function(t){return e===t.type})),this.curType=e},onClose:function(){this.visible=!1},handleOk:function(){var e=this,t=this;this.form.validateFields((function(s,r){if(!s){t.confirmLoading=!0,e.curRecord.configs=r.configs;var o=Object.assign({},e.curRecord),n=Object(a["d"])(e.url.update,o);n.then((function(e){200===e.code?(t.$message.success(e.message),t.$emit("ok")):t.$message.warning(e.message)})).finally((function(){t.confirmLoading=!1,t.onClose()}))}}))},filterOption:function(e,t){return!!t.componentOptions.children[0].text&&t.componentOptions.children[0].text.toLowerCase().indexOf(e.toLowerCase())>=0},handleCancel:function(){this.onClose()}}}),u=c,l=s("2877"),d=Object(l["a"])(u,n,i,!1,null,"582b6de3",null),f=d.exports,m={name:"Subscribes",components:{AuthModuleModel:f},data:function(){return{loading:!0,data:[]}},filters:{},created:function(){this.loadData()},methods:{loadData:function(){var e=this;this.loading=!0,setTimeout((function(){return Object(a["b"])("/v1/modules/modules",{}).then((function(t){e.data=t.data,e.loading=!1}))}),1e3)},handleCheck:function(e,t){var s=this;t.enable=e;var r=Object(a["d"])("/v1/modules",t);r.then((function(e){200===e.code?s.$message.success(e.message):s.$message.warning(e.message)}))},handleUpdate:function(e){switch(e.key){case"AUTH-MODULE":this.$refs.AuthModuleModel.update(e)}}}},h=m,p=(s("d45f"),Object(l["a"])(h,r,o,!1,null,"6957c265",null));t["default"]=p.exports},d45f:function(e,t,s){"use strict";s("042a")}}]);