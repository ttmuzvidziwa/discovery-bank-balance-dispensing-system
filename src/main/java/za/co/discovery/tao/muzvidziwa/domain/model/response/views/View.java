package za.co.discovery.tao.muzvidziwa.domain.model.response.views;

/**
 * This class defines the JSON views used for serializing and deserializing
 * different parts of the response in the API.
 *
 * The views are used to control which fields are included in the JSON output
 * based on the context of the request.
 */
public class View {
    public interface Transactional {}
    public interface Currency {}
    public interface Withdrawal {}
}