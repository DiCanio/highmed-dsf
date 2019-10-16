package org.highmed.dsf.bpe.listener;

import java.util.ArrayList;
import java.util.HashMap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.hl7.fhir.r4.model.Task;

public class StartListener implements ExecutionListener
{
	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		// Task.status.INPROGRESS is set in the TaskHandler when the task is received
		// start of main process instance if no parent available or the parent id is same as the actual process id
		if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
		{
			Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			execution.setVariable(Constants.VARIABLE_LEADING_TASK, task);

			execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, new ArrayList<OutputWrapper>());
		}

		if (!execution.getVariableNames().contains(Constants.VARIABLE_IS_CALL_ACTIVITY)) {
			execution.setVariable(Constants.VARIABLE_IS_CALL_ACTIVITY, false);
		}
	}
}
