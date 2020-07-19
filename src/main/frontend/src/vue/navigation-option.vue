<template>
    <li href="#" :class="'list-group-item' + (this.$store.state.currentTab == targetSection ?' active' : '')" :aria-expanded="(this.$store.state.currentTab == targetSection ? 'true' : 'false')">
        <a @click.stop="selectOption"><i :class="'fa fa-' + icon + ' fa-fw'"></i> {{ label }}</a>
    </li>
</template>

<script>
    export default {
        name: 'navigation-option',
        props: {
            targetSection : {
                type: String
                ,required: true
            },label: {
                type: String
                ,required: true
            },icon: {
                type: String
                ,required: true
            }
        },
        methods: {
            selectOption() {
                this.$store.commit('changeTab',this.targetSection)
            }
        }
    }
</script>

<style scoped>
</style>