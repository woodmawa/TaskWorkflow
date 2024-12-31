package org.softwood.graph

import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus
import org.softwood.taskTypes.TaskTrait

/**
 * working state for and data for vertex node in graph
 *
 */
class TaskVertex extends Vertex implements TaskTrait {

    TaskVertex (String name, Class type  ) {
        super (name, type)
    }

    /**
     * determines if all the predecessor tasks for a join have completed
     * @return
     */
    boolean isReadyToExecute() {
        // Special logic for join nodes, check all predessors have completed
        if (taskType == "JoinTask") {
            return requiredPredecessors.every { predecessorName ->
                def predecessor = parentInstance.taskCache
                //graph.lookupVertexByTaskName(predecessorName)
                predecessor?.status == TaskStatus.COMPLETED
            }
        }
        // Regular nodes just need to be NOT_STARTED
        return status == TaskStatus.NOT_STARTED
    }



    @Override
    void setTaskType(String taskType) {

    }

    @Override
    void setTaskCategory(TaskCategories taskCategory) {

    }
}
