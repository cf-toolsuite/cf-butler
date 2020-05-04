package org.cloudfoundry.client.v3;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The lifecycle type
 */
@SuppressWarnings({"all"})
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
public final class Lifecycle extends org.cloudfoundry.client.v3._Lifecycle {
  private final LifecycleData data;
  private final LifecycleType type;

  private Lifecycle(Lifecycle.Builder builder) {
    this.data = builder.data;
    this.type = builder.type;
  }

  /**
   * The data for the lifecycle
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
  @JsonSubTypes({@JsonSubTypes.Type(name = "buildpack", value = BuildpackData.class), @JsonSubTypes.Type(name = "docker", value = DockerData.class), @JsonSubTypes.Type(name = "kpack", value = DockerData.class)})
  @JsonProperty("data")
  @Override
  public LifecycleData getData() {
    return data;
  }

  /**
   * The type
   */
  @JsonProperty("type")
  @Override
  public LifecycleType getType() {
    return type;
  }

  /**
   * This instance is equal to all instances of {@code Lifecycle} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof Lifecycle
        && equalTo((Lifecycle) another);
  }

  private boolean equalTo(Lifecycle another) {
    return data.equals(another.data)
        && type.equals(another.type);
  }

  /**
   * Computes a hash code from attributes: {@code data}, {@code type}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + data.hashCode();
    h += (h << 5) + type.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code Lifecycle} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "Lifecycle{"
        + "data=" + data
        + ", type=" + type
        + "}";
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "_Lifecycle", generator = "Immutables")
  @Deprecated
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json extends org.cloudfoundry.client.v3._Lifecycle {
    LifecycleData data;
    LifecycleType type;
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(name = "buildpack", value = BuildpackData.class), @JsonSubTypes.Type(name = "docker", value = DockerData.class), @JsonSubTypes.Type(name = "kpack", value = DockerData.class)})
    @JsonProperty("data")
    public void setData(LifecycleData data) {
      this.data = data;
    }
    @JsonProperty("type")
    public void setType(LifecycleType type) {
      this.type = type;
    }
    @Override
    public LifecycleData getData() { throw new UnsupportedOperationException(); }
    @Override
    public LifecycleType getType() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static Lifecycle fromJson(Json json) {
    Lifecycle.Builder builder = Lifecycle.builder();
    if (json.data != null) {
      builder.data(json.data);
    }
    if (json.type != null) {
      builder.type(json.type);
    }
    return builder.build();
  }

  /**
   * Creates a builder for {@link Lifecycle Lifecycle}.
   * <pre>
   * Lifecycle.builder()
   *    .data(org.cloudfoundry.client.v3.LifecycleData) // required {@link Lifecycle#getData() data}
   *    .type(org.cloudfoundry.client.v3.LifecycleType) // required {@link Lifecycle#getType() type}
   *    .build();
   * </pre>
   * @return A new Lifecycle builder
   */
  public static Lifecycle.Builder builder() {
    return new Lifecycle.Builder();
  }

  /**
   * Builds instances of type {@link Lifecycle Lifecycle}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "_Lifecycle", generator = "Immutables")
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private static final long INIT_BIT_TYPE = 0x2L;
    private long initBits = 0x3L;

    private LifecycleData data;
    private LifecycleType type;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Lifecycle} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(Lifecycle instance) {
      return from((_Lifecycle) instance);
    }

    /**
     * Copy abstract value type {@code _Lifecycle} instance into builder.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    final Builder from(_Lifecycle instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      type(instance.getType());
      return this;
    }

    /**
     * Initializes the value for the {@link Lifecycle#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(name = "buildpack", value = BuildpackData.class), @JsonSubTypes.Type(name = "docker", value = DockerData.class), @JsonSubTypes.Type(name = "kpack", value = DockerData.class)})
    @JsonProperty("data")
    public final Builder data(LifecycleData data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Initializes the value for the {@link Lifecycle#getType() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    @JsonProperty("type")
    public final Builder type(LifecycleType type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Builds a new {@link Lifecycle Lifecycle}.
     * @return An immutable instance of Lifecycle
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public Lifecycle build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new Lifecycle(this);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      return "Cannot build Lifecycle, some of required attributes are not set " + attributes;
    }
  }
}
