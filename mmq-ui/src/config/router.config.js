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
            },
            {
                path: '/modules',
                name: 'modules',
                component: RouteView,
                meta: { title: 'menu.modules', keepAlive: true, icon: bxAnaalyse, permission: ['modules'] },
                children: [
                    {
                        path: '/modules/modulesManager',
                        name: 'modules',
                        component: () => import('@/views/modules/ModulesManager'),
                        meta: { title: 'menu.modules.modules', keepAlive: true, permission: ['modules'] }
                    }
                ]
            },
            {
                path: '/account',
                name: 'account',
                component: RouteView,
                meta: { title: 'menu.account', keepAlive: true, icon: bxAnaalyse, permission: ['account'] },
                hidden: true,
                children: [
                    {
                        path: '/account/settings',
                        name: 'setting',
                        component: () => import('@/views/user/AccountSetting'),
                        hidden: true,
                        meta: { title: 'menu.account.settings', keepAlive: true, permission: ['account'] }
                    }
                ]
            }
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
