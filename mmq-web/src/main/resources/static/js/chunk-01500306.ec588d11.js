(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-01500306"],{"0fea":function(e,r,t){"use strict";t.d(r,"c",(function(){return s})),t.d(r,"b",(function(){return o})),t.d(r,"a",(function(){return n}));var a=t("b775");function s(e,r){return Object(a["b"])({url:e,method:"post",data:r})}function o(e,r){return Object(a["b"])({url:e,method:"get",params:r})}function n(e,r){return Object(a["b"])({url:e,method:"delete",params:r})}},5125:function(e,r,t){},"7a56":function(e,r,t){"use strict";t.r(r);var a=function(){var e=this,r=e.$createElement,t=e._self._c||r;return t("a-card",{attrs:{bordered:!1}},[t("div",{staticClass:"table-operator"}),t("a-list",{attrs:{grid:{gutter:24,lg:3,md:2,sm:1,xs:1},"data-source":e.data},scopedSlots:e._u([{key:"renderItem",fn:function(r){return t("a-list-item",{},[r&&void 0!==r.resourceID?[t("a-card",{attrs:{title:r.title}},[t("div",{attrs:{slot:"title"},slot:"title"},[e._v(e._s(r.resourceID))]),t("a",{attrs:{slot:"actions"},on:{click:function(t){return e.handleSave(r)}},slot:"actions"},[e._v("编辑")]),t("a-popconfirm",{attrs:{slot:"actions",title:"Sure to delete?"},on:{confirm:function(){return e.handleDelete(r)}},slot:"actions"},[t("a",{attrs:{href:"javascript:;"}},[e._v("删除")])]),e._v(" "+e._s(e.getDescription(r))+" ")],1)]:[t("a-button",{staticClass:"new-btn",attrs:{type:"dashed"},on:{click:function(r){return e.handleSave({})}}},[t("a-icon",{attrs:{type:"plus"}}),e._v(" 新增资源 ")],1)]],2)}}])}),t("ResourceModel",{ref:"ResourceModel",on:{ok:e.loadData}})],1)},s=[],o=t("0fea"),n=function(){var e=this,r=e.$createElement,t=e._self._c||r;return t("a-drawer",{attrs:{confirmLoading:e.confirmLoading,title:"保存新资源",width:720,visible:e.visible,"body-style":{paddingBottom:"80px"}},on:{close:e.onClose}},[t("a-form",{attrs:{form:e.form,layout:"vertical","hide-required-mark":""}},[t("a-row",{attrs:{gutter:16}},[t("a-form-item",{attrs:{label:"资源类型"}},[t("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["type",{rules:[{required:!0,message:"请选择资源类型"}]}],expression:"[\n            'type',\n            {\n              rules: [{ required: true, message: '请选择资源类型' }],\n            },\n          ]"}],attrs:{placeholder:"请选择资源类型"},on:{change:e.typeChange}},[t("a-select-option",{attrs:{value:"MYSQL"}},[e._v(" Mysql ")]),t("a-select-option",{attrs:{value:"POSTGRESQL"}},[e._v(" Postgresql ")]),t("a-select-option",{attrs:{value:"SQLSERVER"}},[e._v(" SqlServer ")]),t("a-select-option",{attrs:{value:"TDENGINE"}},[e._v(" Tdengine ")]),t("a-select-option",{attrs:{value:"INFLUXDB"}},[e._v(" InfluxDB ")]),t("a-select-option",{attrs:{value:"KAFKA"}},[e._v(" Kafka ")])],1)],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"MYSQL"===e.type||"POSTGRESQL"===e.type||"SQLSERVER"===e.type,expression:"type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER'"}]},[t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"IP"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.ip",{rules:[{required:!0,message:"请输入IP"}]}],expression:"[\n                'resource.ip',\n                {\n                  rules: [{ required: true, message: '请输入IP' }],\n                },\n              ]"}],attrs:{placeholder:"请输入IP"}})],1)],1),t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"PORT"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.port",{rules:[{required:!0,message:"请输入端口"}]}],expression:"[\n                'resource.port',\n                {\n                  rules: [{ required: true, message: '请输入端口' }],\n                },\n              ]"}],attrs:{placeholder:"请输入端口"}})],1)],1)],1),t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"账户"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.username",{rules:[{required:!0,message:"请输入账户"}]}],expression:"[\n                'resource.username',\n                {\n                  rules: [{ required: true, message: '请输入账户' }],\n                },\n              ]"}],attrs:{placeholder:"请输入账户"}})],1)],1),t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"密码"}},[t("a-input-password",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.password",{rules:[{required:!0,message:"请输入密码"}]}],expression:"[\n                'resource.password',\n                {\n                  rules: [{ required: true, message: '请输入密码' }],\n                },\n              ]"}],attrs:{placeholder:"请输入密码"}})],1)],1)],1),t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"数据库"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.databaseName",{rules:[{required:!0,message:"请输入要连接的数据库"}]}],expression:"[\n                'resource.databaseName',\n                {\n                  rules: [{ required: true, message: '请输入要连接的数据库' }],\n                },\n              ]"}],attrs:{placeholder:"请输入要连接的数据库"}})],1)],1),t("a-col",{attrs:{span:12}})],1)],1),t("div",{directives:[{name:"show",rawName:"v-show",value:"KAFKA"===e.type,expression:"type === 'KAFKA'"}]},[t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"Kafka服务"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.server",{rules:[{required:"KAFKA"===e.type,message:"请输入Kafka服务"}]}],expression:"[\n                'resource.server',\n                {\n                  rules: [{ required: type === 'KAFKA' ? true : false, message: '请输入Kafka服务' }],\n                },\n              ]"}],attrs:{placeholder:"请输入Kafka服务"}})],1)],1),t("a-col",{attrs:{span:12}})],1),t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"Kafka账户"}},[t("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.username",{rules:[{required:!1,message:"请输入Kafka账户"}]}],expression:"[\n                'resource.username',\n                {\n                  rules: [{ required: false, message: '请输入Kafka账户' }],\n                },\n              ]"}],attrs:{placeholder:"请输入Kafka账户"}})],1)],1),t("a-col",{attrs:{span:12}},[t("a-form-item",{attrs:{label:"Kafka密码"}},[t("a-input-password",{directives:[{name:"decorator",rawName:"v-decorator",value:["resource.password",{rules:[{required:!1,message:"请输入Kafka密码"}]}],expression:"[\n                'resource.password',\n                {\n                  rules: [{ required: false, message: '请输入Kafka密码' }],\n                },\n              ]"}],attrs:{placeholder:"请输入Kafka密码"}})],1)],1)],1)],1),t("a-row",{attrs:{gutter:16}},[t("a-col",{attrs:{span:24}},[t("a-form-item",{attrs:{label:"备注"}},[t("a-textarea",{directives:[{name:"decorator",rawName:"v-decorator",value:["description",{rules:[{required:!1,message:"请输入备注"}]}],expression:"[\n              'description',\n              {\n                rules: [{ required: false, message: '请输入备注' }],\n              },\n            ]"}],attrs:{rows:4,placeholder:"请输入备注"}})],1)],1)],1)],1),t("div",{style:{position:"absolute",right:0,bottom:0,width:"100%",borderTop:"1px solid #e9e9e9",padding:"10px 16px",background:"#fff",textAlign:"right",zIndex:1}},[t("a-button",{style:{marginRight:"8px"},on:{click:e.onClose}},[e._v(" Cancel ")]),t("a-button",{attrs:{type:"primary"},on:{click:e.handleOk}},[e._v(" Submit ")])],1)],1)},i=[],c=(t("a4d3"),t("e01a"),t("d3b7"),{data:function(){return{title:"操作",visible:!1,curResourceID:null,confirmLoading:!1,type:"",form:this.$form.createForm(this),url:{save:"/v1/resources"}}},created:function(){},methods:{save:function(e){this.form.resetFields(),this.visible=!0,e&&(this.curResourceID=e.resourceID,this.setFieldsValueByType(e.type,e))},setFieldsValueByType:function(e,r){var t=this;switch(this.type=e,e){case"MYSQL":this.$nextTick((function(){t.form.setFieldsValue({resourceID:r.resourceID,type:e,description:r.description,resource:{ip:r.resource.ip,port:r.resource.port,databaseName:r.resource.databaseName,password:r.resource.password,username:r.resource.username}})}));break;case"POSTGRESQL":this.$nextTick((function(){t.form.setFieldsValue({resourceID:r.resourceID,type:e,description:r.description,resource:{ip:r.resource.ip,port:r.resource.port,databaseName:r.resource.databaseName,password:r.resource.password,username:r.resource.username}})}));break;case"SQLSERVER":this.$nextTick((function(){t.form.setFieldsValue({resourceID:r.resourceID,type:e,description:r.description,resource:{ip:r.resource.ip,port:r.resource.port,databaseName:r.resource.databaseName,password:r.resource.password,username:r.resource.username}})}));break;case"TDENGINE":break;case"KAFKA":this.$nextTick((function(){t.form.setFieldsValue({resourceID:r.resourceID,type:e,description:r.description,resource:{server:r.resource.server,password:r.resource.password,username:r.resource.username}})}));break}},typeChange:function(e){this.type=e},onClose:function(){this.visible=!1},handleOk:function(){var e=this,r=this;this.form.validateFields((function(t,a){if(!t){r.confirmLoading=!0;var s=Object.assign({},a);s.resourceID=e.curResourceID;var n=Object(o["c"])(e.url.save,s);n.then((function(e){200===e.code?(r.$message.success(e.message),r.$emit("ok")):r.$message.warning(e.message)})).finally((function(){r.confirmLoading=!1,r.onClose()}))}}))},filterOption:function(e,r){return!!r.componentOptions.children[0].text&&r.componentOptions.children[0].text.toLowerCase().indexOf(e.toLowerCase())>=0},handleCancel:function(){this.onClose()}}}),u=c,l=t("2877"),d=Object(l["a"])(u,n,i,!1,null,"7209a0d7",null),p=d.exports,m={name:"Subscribes",components:{ResourceModel:p},data:function(){return{loading:!0,data:[]}},filters:{},created:function(){this.loadData()},methods:{loadData:function(){var e=this;return Object(o["b"])("/v1/resources/resources",{}).then((function(r){e.data=r.data,e.data.unshift({})}))},getDescription:function(e){return"资源ID:"+e.resourceID+" 资源类型:"+e.type+this.getResourceContentByType(e.resource,e.type)},getResourceContentByType:function(e,r){switch(r){case"MYSQL":return" ip:"+e.ip+" port:"+e.port+" 数据库名称:"+e.databaseName;case"POSTGRESQL":return" ip:"+e.ip+" port:"+e.port+" 数据库名称:"+e.databaseName;case"SQLSERVER":return" ip:"+e.ip+" port:"+e.port+" 数据库名称:"+e.databaseName;case"KAFKA":return" Kafka服务:"+e.server;default:return""}},handleSave:function(e){this.$refs.ResourceModel.save(e)},handleDelete:function(e){var r=this;Object(o["a"])("/v1/resources",{resourceID:e.resourceID}).then((function(e){200===e.code?(r.$message.info(e.message),r.loadData()):r.$message.info(e.message)}))}}},f=m,v=(t("d068"),Object(l["a"])(f,a,s,!1,null,"1e13103f",null));r["default"]=v.exports},d068:function(e,r,t){"use strict";t("5125")}}]);