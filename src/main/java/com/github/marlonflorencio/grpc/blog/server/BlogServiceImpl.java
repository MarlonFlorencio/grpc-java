package com.github.marlonflorencio.grpc.blog.server;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.BlogRequest;
import com.proto.blog.BlogResponse;
import com.proto.blog.BlogId;
import com.proto.blog.ListBlogRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    MongoCredential credential = MongoCredential.createCredential("root", "admin", "secret".toCharArray());

    MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
            .credential(credential)
                    .build());

    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");


    @Override
    public void createBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {

        System.out.println("Received Create Blog request");

        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting blog...");
        // we insert (create) the document in mongoDB
        collection.insertOne(doc);


        // we retrieve the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: " + id);

        BlogResponse response = BlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }


    @Override
    public void readBlog(BlogId request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received Read Blog request");

        String blogId = request.getBlogId();

        System.out.println("Searching for a blog");
        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            System.out.println("Blog not found");
            // we don't have a match
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog found, sending response");
            Blog blog = documentToBlog(result);

            responseObserver.onNext(BlogResponse.newBuilder().setBlog(blog).build());

            responseObserver.onCompleted();
        }

    }

    @Override
    public void updateBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received Update Blog request");

        Blog blog = request.getBlog();

        String blogId = blog.getId();

        System.out.println("Searching for a blog so we can update it");
        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            System.out.println("Blog not found");
            // we don't have a match
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blogId));

            System.out.println("Replacing blog in database...");

            collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

            System.out.println("Replaced! Sending as a response");
            responseObserver.onNext(
                    BlogResponse.newBuilder()
                            .setBlog(documentToBlog(replacement))
                            .build()
            );

            responseObserver.onCompleted();
        }
    }

    private Blog documentToBlog(Document document){
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<BlogId> responseObserver) {
        System.out.println("Received Delete Blog Request");

        String blogId = request.getBlogId();
        DeleteResult result = null;
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result.getDeletedCount() == 0) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog was deleted");
            responseObserver.onNext(BlogId.newBuilder()
                    .setBlogId(blogId)
                    .build());

            responseObserver.onCompleted();
        }

    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        System.out.println("Received List Blog Request");

        collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
                BlogResponse.newBuilder().setBlog(documentToBlog(document)).build()
        ));

        responseObserver.onCompleted();
    }
}
