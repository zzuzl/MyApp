package cn.zlihj.dao;

import cn.zlihj.domain.Exam;
import cn.zlihj.domain.Subject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubjectDao {
    void insertOrUpdate(Subject subject);

    List<Subject> list(@Param("examId") Integer examId);

    int updateExam(Exam exam);

    int deleteOthers(@Param("examId") Integer examId,
                     @Param("orders") List<Integer> orders);

    List<Integer> selectSubjectIds(@Param("examId") Integer examId);

    List<Subject> selectSubjectByIds(@Param("ids") List<Integer> ids);

    Exam getById(@Param("examId") Integer examId);
}
