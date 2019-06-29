package sp;

import java.io.Serializable;


/**
 * An event representing user activity.
 */
public class UserEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long _time;
    private String _customerId;
    private String _page;
    private Boolean _isCartEmpty;


    public UserEvent(Long time, String customerId, String page, Boolean isCartEmpty) {
        setTime(time);
        setCustomerId(customerId);
        setPage(page);
        setIsCartEmpty(isCartEmpty);
    }


    public Long getTime() {
        return _time;
    }


    public void setTime(Long time) {
        _time = time;
    }


    public String getCustomerId() {
        return _customerId;
    }


    public void setCustomerId(String customerId) {
        _customerId = customerId;
    }


    public String getPage() {
        return _page;
    }


    public void setPage(String page) {
        _page = page;
    }


    public Boolean getIsCartEmpty() {
        return _isCartEmpty;
    }


    public void setIsCartEmpty(Boolean isCartEmpty) {
        _isCartEmpty = isCartEmpty;
    }


    @Override
    public String toString() {
        return "time: " + _time + ", customer id: " + _customerId +
            ", page: " + _page + ", is cart empty: " + _isCartEmpty;
    }
}
