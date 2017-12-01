select * from scott.EMP ORDER by ENAME;

desc scott.EMP;

select EMPNO, ENAME, JOB, MGR, HIREDATE from SCOTT.EMP where ENAME like 'A%';

alter user scott account unlock;

alter user scott identified by tiger;

select * from V$SESSION
where username = 'SCOTT';