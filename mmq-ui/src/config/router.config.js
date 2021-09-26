// eslint-disable-next-line
import { UserLayout, BasicLayout, BlankLayout } from '@/layouts'
import { bxAnaalyse } from '@/core/icons'

const RouteView = {
    name: 'RouteView',
    render: h => h('router-view')
}

export const asyncRouterMap = [
    {
        path: '/',
        name: 'index',
        component: BasicLayout,
        meta: { title: 'menu.home' },
        redirect: '/dashboard/monitor',
        children: [
            // dashboard
            {
                path: '/dashboard',
                name: 'dashboard',
                redirect: '/dashboard/monitor',
                component: RouteView,
                meta: { title: 'menu.dashboard', keepAlive: true, icon: bxAnaalyse, permission: ['dashboard'] },
                children: [
                    // 外部链接
                    {
                        path: '/dashboard/monitor',
                        name: 'Monitor',
                        component: () => import('@/views/dashboard/Monitor'),
                        meta: { title: 'menu.dashboard.monitor', keepAlive: false }
                    },
                    {
                        path: '/dashboard/clients/:pageNo([1-9]\\d*)?',
                        name: 'clients',
                        component: () => import('@/views/dashboard/Clients'),
                        meta: { title: 'menu.dashboard.clients', keepAlive: false, permission: ['dashboard'] }
                    },
                    {
                        path: '/dashboard/subscription',
                        name: 'subscription',
                        component: () => import('@/views/dashboard/Subscription'),
                        meta: { title: 'menu.dashboard.subscription', keepAlive: true, permission: ['dashboard'] }
                    }
                ]
            },
            {
                path: '/ruleEngine',
                name: 'ruleEngine',
                component: RouteView,
                meta: { title: 'menu.ruleEngine', keepAlive: true, icon: bxAnaalyse, permission: ['ruleEngine'] },
                children: [
                    {
                        path: '/ruleEngine/resources',
                        name: 'resources',
                        component: () => import('@/views/ruleEngine/Resources'),
                        meta: { title: 'menu.ruleEngine.resources', keepAlive: true, permission: ['ruleEngine'] }
                    },
                    {
                        path: '/ruleEngine/ruleEngine',
                        name: 'ruleEngine',
                        component: () => import('@/views/ruleEngine/RuleEngine'),
                        meta: { title: 'menu.ruleEngine.ruleEngine', keepAlive: true, permission: ['ruleEngine'] }
                    },
                    {
                        path: '/ruleEngine/RuleEngineModel',
                        name: 'RuleEngineModel',
                        props: true,
                        component: () => import('@/views/ruleEngine/modules/RuleEngineModel'),
                        hidden: true
                    }

                ]
            }
            // other
            /*
            {
              path: '/other',
              name: 'otherPage',
              component: PageView,
              meta: { title: '其他组件', icon: 'slack', permission: [ 'dashboard' ] },
              redirect: '/other/icon-selector',
              children: [
                {
                  path: '/other/icon-selector',
                  name: 'TestIconSelect',
                  component: () => import('@/views/other/IconSelectorView'),
                  meta: { title: 'IconSelector', icon: 'tool', keepAlive: true, permission: [ 'dashboard' ] }
                },
                {
                  path: '/other/list',
                  component: RouteView,
                  meta: { title: '业务布局', icon: 'layout', permission: [ 'support' ] },
                  redirect: '/other/list/tree-list',
                  children: [
                    {
                      path: '/other/list/tree-list',
                      name: 'TreeList',
                      component: () => import('@/views/other/TreeList'),
                      meta: { title: '树目录表格', keepAlive: true }
                    },
                    {
                      path: '/other/list/edit-table',
                      name: 'EditList',
                      component: () => import('@/views/other/TableInnerEditList'),
                      meta: { title: '内联编辑表格', keepAlive: true }
                    },
                    {
                      path: '/other/list/user-list',
                      name: 'UserList',
                      component: () => import('@/views/other/UserList'),
                      meta: { title: '用户列表', keepAlive: true }
                    },
                    {
                      path: '/other/list/role-list',
                      name: 'RoleList',
                      component: () => import('@/views/other/RoleList'),
                      meta: { title: '角色列表', keepAlive: true }
                    },
                    {
                      path: '/other/list/system-role',
                      name: 'SystemRole',
                      component: () => import('@/views/role/RoleList'),
                      meta: { title: '角色列表2', keepAlive: true }
                    },
                    {
                      path: '/other/list/permission-list',
                      name: 'PermissionList',
                      component: () => import('@/views/other/PermissionList'),
                      meta: { title: '权限列表', keepAlive: true }
                    }
                  ]
                }
              ]
            }
            */
        ]
    },
    {
        path: '*',
        redirect: '/404',
        hidden: true
    }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
    {
        path: '/user',
        component: UserLayout,
        redirect: '/user/login',
        hidden: true,
        children: [
            {
                path: 'login',
                name: 'login',
                component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
            },
            {
                path: 'register',
                name: 'register',
                component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
            },
            {
                path: 'register-result',
                name: 'registerResult',
                component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
            },
            {
                path: 'recover',
                name: 'recover',
                component: undefined
            }
        ]
    },

    {
        path: '/404',
        component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
    }
]
