package cn.zlihj.dto;

public class Result {
    private boolean success = false;
    private String msg;
    private Object data;

    public Result() {
    }

    public Result(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static Result successResult() {
        return new Result(true);
    }

    public static Result errorResult(String msg) {
        Result result = new Result(false);
        result.setMsg(msg);
        return result;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                '}';
    }
}
