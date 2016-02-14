package pl.edu.agh.messages

import pl.edu.agh.workflow_patterns.merge.Merge

sealed trait MergeMessage
case class PropagateDataForMerge[T, K](elem: Merge[T, K]) extends MergeMessage
