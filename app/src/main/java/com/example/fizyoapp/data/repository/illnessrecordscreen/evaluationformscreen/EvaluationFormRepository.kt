package com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationForm
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import kotlinx.coroutines.flow.Flow

interface EvaluationFormRepository {
    fun getEvaluationForms(userId: String): Flow<Resource<List<EvaluationForm>>>
    fun getEvaluationFormById(formId: String): Flow<Resource<EvaluationForm>>
    fun saveFormResponse(formResponse: FormResponse): Flow<Resource<Boolean>>
    fun getFormResponsesByUser(userId: String): Flow<Resource<List<FormResponse>>>
    fun getFormResponseById(responseId: String): Flow<Resource<FormResponse>>
    fun shareFormResponse(responseId: String, receiverId: String): Flow<Resource<Boolean>>
    fun deleteFormResponse(responseId: String): Flow<Resource<Boolean>>
    fun initializeDefaultForms(): Flow<Resource<Boolean>>

}