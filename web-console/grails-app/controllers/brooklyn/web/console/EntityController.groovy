package brooklyn.web.console

import grails.converters.JSON
import grails.plugins.springsecurity.Secured

import brooklyn.entity.Entity
import brooklyn.web.console.entity.EntitySummary
import brooklyn.web.console.entity.JsTreeNodeImpl

@Secured(['ROLE_ADMIN'])
class EntityController {

    def entityService

    def index = {}

    def list = {
        render(toEntitySummaries(entityService.getAllEntities()) as JSON)
    }

    def search = {
        render(toEntitySummaries(entityService.getEntitiesMatchingCriteria(params.name, params.id, params.applicationId)) as JSON)
    }

    def jstree = {
        Map<String, JsTreeNodeImpl> nodeMap = [:]
        Collection<Entity> all = entityService.getAllEntities()
        JsTreeNodeImpl root = new JsTreeNodeImpl("root", ".", "root", true)

        all.each { nodeMap.put(it.id, new JsTreeNodeImpl(it, true)) }

        all.each {
            entity ->
            entityService.getTheKiddies(entity).each {
                child -> nodeMap[entity.id].children.add(nodeMap[child.id])
            }
        }

//        // TODO Place matches at the root of our tree view (iff an ancestor isn't already present)
//        Collection<Entity> matches = entityService.getEntitiesMatchingCriteria(params.name, params.id, params.applicationId);
//        matches.each { match ->
//            if (!entityService.isChildOf(match, matches)) {
//                root.children.add(nodeMap[match.id])
//            }
//        }

        entityService.getTopLevelEntities().each {
            root.children.add(nodeMap[it.id])
        }

        render(root as JSON)
    }

    private Set<EntitySummary> toEntitySummaries(Collection<Entity> entities) {
        entities.collect {  new EntitySummary(it) }
    }
}
