// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Empty.proto

package qxmobile.protobuf;

public final class EmptyProtos {
  private EmptyProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface EmptyMessageOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code qxmobile.protobuf.EmptyMessage}
   */
  public static final class EmptyMessage extends
      com.google.protobuf.GeneratedMessage
      implements EmptyMessageOrBuilder {
    // Use EmptyMessage.newBuilder() to construct.
    private EmptyMessage(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private EmptyMessage(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final EmptyMessage defaultInstance;
    public static EmptyMessage getDefaultInstance() {
      return defaultInstance;
    }

    public EmptyMessage getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private EmptyMessage(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return qxmobile.protobuf.EmptyProtos.internal_static_qxmobile_protobuf_EmptyMessage_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return qxmobile.protobuf.EmptyProtos.internal_static_qxmobile_protobuf_EmptyMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              qxmobile.protobuf.EmptyProtos.EmptyMessage.class, qxmobile.protobuf.EmptyProtos.EmptyMessage.Builder.class);
    }

    public static com.google.protobuf.Parser<EmptyMessage> PARSER =
        new com.google.protobuf.AbstractParser<EmptyMessage>() {
      public EmptyMessage parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new EmptyMessage(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<EmptyMessage> getParserForType() {
      return PARSER;
    }

    private void initFields() {
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static qxmobile.protobuf.EmptyProtos.EmptyMessage parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(qxmobile.protobuf.EmptyProtos.EmptyMessage prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code qxmobile.protobuf.EmptyMessage}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements qxmobile.protobuf.EmptyProtos.EmptyMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return qxmobile.protobuf.EmptyProtos.internal_static_qxmobile_protobuf_EmptyMessage_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return qxmobile.protobuf.EmptyProtos.internal_static_qxmobile_protobuf_EmptyMessage_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                qxmobile.protobuf.EmptyProtos.EmptyMessage.class, qxmobile.protobuf.EmptyProtos.EmptyMessage.Builder.class);
      }

      // Construct using qxmobile.protobuf.EmptyProtos.EmptyMessage.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return qxmobile.protobuf.EmptyProtos.internal_static_qxmobile_protobuf_EmptyMessage_descriptor;
      }

      public qxmobile.protobuf.EmptyProtos.EmptyMessage getDefaultInstanceForType() {
        return qxmobile.protobuf.EmptyProtos.EmptyMessage.getDefaultInstance();
      }

      public qxmobile.protobuf.EmptyProtos.EmptyMessage build() {
        qxmobile.protobuf.EmptyProtos.EmptyMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public qxmobile.protobuf.EmptyProtos.EmptyMessage buildPartial() {
        qxmobile.protobuf.EmptyProtos.EmptyMessage result = new qxmobile.protobuf.EmptyProtos.EmptyMessage(this);
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof qxmobile.protobuf.EmptyProtos.EmptyMessage) {
          return mergeFrom((qxmobile.protobuf.EmptyProtos.EmptyMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(qxmobile.protobuf.EmptyProtos.EmptyMessage other) {
        if (other == qxmobile.protobuf.EmptyProtos.EmptyMessage.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        qxmobile.protobuf.EmptyProtos.EmptyMessage parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (qxmobile.protobuf.EmptyProtos.EmptyMessage) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      // @@protoc_insertion_point(builder_scope:qxmobile.protobuf.EmptyMessage)
    }

    static {
      defaultInstance = new EmptyMessage(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:qxmobile.protobuf.EmptyMessage)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_qxmobile_protobuf_EmptyMessage_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_qxmobile_protobuf_EmptyMessage_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\013Empty.proto\022\021qxmobile.protobuf\"\016\n\014Empt" +
      "yMessageB\rB\013EmptyProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_qxmobile_protobuf_EmptyMessage_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_qxmobile_protobuf_EmptyMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_qxmobile_protobuf_EmptyMessage_descriptor,
              new java.lang.String[] { });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
