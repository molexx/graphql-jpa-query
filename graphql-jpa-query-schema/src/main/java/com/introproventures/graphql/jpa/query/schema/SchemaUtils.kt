package com.supapass.adminapi

import com.introproventures.graphql.jpa.query.schema.JavaScalars
import com.introproventures.graphql.jpa.query.schema.NamingStrategy
import graphql.schema.*
import mu.KotlinLogging
import java.util.ArrayList
import java.util.HashMap
import java.util.stream.Stream
import javax.persistence.metamodel.*
import kotlin.streams.toList


/**
 * 
 * Mostly copied from GraphQLJpaSchemaBuilder because its members are private and don't handle Input types
 * 
 */


object SchemaUtils {
    
    private val logger = KotlinLogging.logger {}

    fun getMutationField(fieldName: String, fieldDescription: String, outputType: GraphQLTypeReference, inputType: GraphQLInputObjectType, datafetcher: DataFetcher<Any?>) : GraphQLFieldDefinition {
              
        return(GraphQLFieldDefinition.newFieldDefinition()
                //.field(newInputObjectField()
                .name(fieldName)
                .description(fieldDescription)
                .type(outputType)
                .argument(GraphQLArgument.newArgument()
                        .name("data")
                        //.type(getInputObjectType(userDaoEntity))
                        //.type(inputType)
                        .type(inputType)
                        //.type(GraphQLNonNull(GraphQLTypeReference(returnTypeName)))
                        //.type(GraphQLTypeReference(returnTypeName) as GraphQLInputType)   // graphql.schema.GraphQLObjectType cannot be cast to graphql.schema.GraphQLInputType
                )
                .dataFetcher(datafetcher)
                .build()
                )
    }
    
    private val entityInputCache = HashMap<EntityType<*>, GraphQLInputObjectType>()
    
    fun getInputObjectType(entityType: EntityType<*>): GraphQLInputObjectType {
        if (entityInputCache.containsKey(entityType))
            return entityInputCache!!.get(entityType)!!
        

        val objectType = GraphQLInputObjectType.newInputObject()
                .name(entityType.name + "Input")
                //.description(getSchemaDescription(entityType.javaType))
                //.field(field)
                .fields(entityType.attributes.mapNotNull { getInputObjectField(it) }
                        //.toList()
                        //.collect<List<GraphQLFieldDefinition>, Any>(Collectors.toList())
                )
                .build()

        entityInputCache.putIfAbsent(entityType, objectType)

        return objectType
    }
    
    /*
    private fun getObjectField(attribute: Attribute<*, *>): GraphQLFieldDefinition {
        val type = getAttributeType(attribute)

        if (type is GraphQLOutputType) {
            val arguments = ArrayList<GraphQLArgument>()
            var dataFetcher: DataFetcher<*> = PropertyDataFetcher<Any>(attribute.name)

            // Get the fields that can be queried on (i.e. Simple Types, no Sub-Objects)
            if (attribute is SingularAttribute<*, *>
                    && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC
                    && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {

                val foreignType = attribute.type as EntityType<*>
                val attributes = findBasicAttributes(foreignType.attributes)

                // TODO fix page count query
                //arguments.add(getWhereArgument(foreignType))

            } //  Get Sub-Objects fields queries via DataFetcher     
            else if (attribute is PluralAttribute<*, *, *> && (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY)) {
                val declaringType = attribute.declaringType as EntityType<*>
                val elementType = attribute.elementType as EntityType<*>

                //arguments.add(getWhereArgument(elementType))
                //dataFetcher = GraphQLJpaOneToManyDataFetcher(entityManager, declaringType, attribute)
            }

            return GraphQLFieldDefinition.newFieldDefinition()
                    .name(attribute.name)
                    //.description(getSchemaDescription(attribute.javaMember))
                    .type(type as GraphQLOutputType)
                    .dataFetcher(dataFetcher)
                    .argument(arguments)
                    .build()
        }

        throw IllegalArgumentException("Attribute $attribute cannot be mapped as an Output Argument")
    }
    */
    
    
    private fun getInputObjectField(attribute: Attribute<*, *>): GraphQLInputObjectField? {
        var type = getInputAttributeType(attribute)
        
        if (type == null) {
            return null
        }
        
        //reference to other table - e.g. Content has userDaoByUpdatedBy
        if (type is GraphQLTypeReference) {
            logger.error(Throwable()) { "getInputAttributeType() returned unexpected type $type for '${attribute.name}'"}
            return null
        }
        
        //nested foreign collections - e.g. Content has commentDAOs
        if (type is GraphQLList) {
            logger.error(Throwable()) { "getInputAttributeType() returned unexpected type $type for '${attribute.name}'"}
            return null
        }
        
        
        logger.debug { "getInputObjectField(): type: '${type}', attribute '${attribute.name}': ${attribute}" }
        
        if (type is GraphQLInputType) {
            
            //logger.debug { "getInputObjectField(): type is a GraphQLInputType!" }
            //val arguments = ArrayList<GraphQLArgument>()
            //var dataFetcher: DataFetcher<*> = PropertyDataFetcher<Any>(attribute.name)

            /*
            // Only add the orderBy argument for basic attribute types
            if (attribute is SingularAttribute<*, *> && attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                arguments.add(GraphQLArgument.newArgument()
                        .name(ORDER_BY_PARAM_NAME)
                        .description("Specifies field sort direction in the query results.")
                        .type(orderByDirectionEnum)
                        .build()
                )
            }*/

            // Get the fields that can be queried on (i.e. Simple Types, no Sub-Objects)
            if (attribute is SingularAttribute<*, *>
                    && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC
                    && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {
                //logger.debug { "getInputObjectField(): attribute in if part 1" }

                val foreignType = attribute.type as EntityType<*>
                val attributes = findBasicAttributes(foreignType.attributes)

                // TODO fix page count query
                //arguments.add(getWhereArgument(foreignType))

            } //  Get Sub-Objects fields queries via DataFetcher     
            else if (attribute is PluralAttribute<*, *, *> && (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY)) {
                //logger.debug { "getInputObjectField(): attribute in if part 2" }
                val declaringType = attribute.declaringType as EntityType<*>
                val elementType = attribute.elementType as EntityType<*>

                //arguments.add(getWhereArgument(elementType))
                //dataFetcher = GraphQLJpaOneToManyDataFetcher(entityManager, declaringType, attribute)
                return(null)
            } else {
                //logger.debug { "getInputObjectField(): attribute in else. Type: $type" }
            }
            
            return GraphQLInputObjectField.newInputObjectField()
                    .name(attribute.name)
                    //.description(getSchemaDescription(attribute.javaMember))
                    .type(type)
                    //.dataFetcher(dataFetcher)
                    //.argument(arguments)
                    .build()
        }

        throw IllegalArgumentException("Attribute $attribute cannot be mapped as an Input Argument")
    }

    private fun findBasicAttributes(attributes: Collection<Attribute<*, *>>): Stream<Attribute<*, *>> {
        return attributes.stream().filter { it -> it.persistentAttributeType == Attribute.PersistentAttributeType.BASIC }
    }

    /*
    private fun getAttributeType(attribute: Attribute<*, *>): GraphQLType {

        if (isBasic(attribute)) {
            return getGraphQLTypeFromJavaType(attribute.javaType)
        } else if (isEmbeddable(attribute)) {
            val embeddableType = (attribute as SingularAttribute<*, *>).type as EmbeddableType<*>
            return getEmbeddableType(embeddableType)
        } else if (isToMany(attribute)) {
            val foreignType = (attribute as PluralAttribute<*, *, *>).elementType as EntityType<*>
            return GraphQLList(GraphQLTypeReference(foreignType.name))
        } else if (isToOne(attribute)) {
            val foreignType = (attribute as SingularAttribute<*, *>).type as EntityType<*>
            return GraphQLTypeReference(foreignType.name)
        } else if (isElementCollection(attribute)) {
            val foreignType = (attribute as PluralAttribute<*, *, *>).elementType

            if (foreignType.persistenceType == javax.persistence.metamodel.Type.PersistenceType.BASIC) {
                return GraphQLList(getGraphQLTypeFromJavaType(foreignType.javaType))
            }
        }

        val declaringType = attribute.declaringType.javaType.name // fully qualified name of the entity class
        val declaringMember = attribute.javaMember.name // field name in the entity class

        throw UnsupportedOperationException(
                "Attribute could not be mapped to GraphQL: field '$declaringMember' of entity class '$declaringType'")
    }
    */
    
    
    private fun getInputAttributeType(attribute: Attribute<*, *>): GraphQLInputType? {

        if (isBasic(attribute)) {
            return getGraphQLInputTypeFromJavaType(attribute.javaType)
        } else if (isEmbeddable(attribute)) {
            val embeddableType = (attribute as SingularAttribute<*, *>).type as EmbeddableType<*>
            return getEmbeddableInputType(embeddableType)
        } else if (isToMany(attribute)) {
            //val foreignType = (attribute as PluralAttribute<*, *, *>).elementType as EntityType<*>
            //return GraphQLList(GraphQLTypeReference(foreignType.name))
            return(null)
        } else if (isToOne(attribute)) {
            val foreignType = (attribute as SingularAttribute<*, *>).type as EntityType<*>
            //return GraphQLTypeReference(foreignType.name)
            val idType = foreignType.idType
            val idClass= idType.javaType
            return getGraphQLInputTypeFromJavaType(idClass)
        } else if (isElementCollection(attribute)) {
            val foreignType = (attribute as PluralAttribute<*, *, *>).elementType

            if (foreignType.persistenceType == javax.persistence.metamodel.Type.PersistenceType.BASIC) {
                return GraphQLList(getGraphQLTypeFromJavaType(foreignType.javaType))
            }
        }

        val declaringType = attribute.declaringType.javaType.name // fully qualified name of the entity class
        val declaringMember = attribute.javaMember.name // field name in the entity class

        throw UnsupportedOperationException(
                "Attribute could not be mapped to GraphQL: field '$declaringMember' of entity class '$declaringType'")
    }

    var namingStrategy: NamingStrategy = object : NamingStrategy {

    }
    private val embeddableCache = HashMap<EmbeddableType<*>, GraphQLObjectType>()
    private fun getEmbeddableType(embeddableType: EmbeddableType<*>): GraphQLObjectType {
        if (embeddableCache.containsKey(embeddableType))
            return embeddableCache.get(embeddableType)!!

        val embeddableTypeName = namingStrategy.singularize(embeddableType.javaType.simpleName) + "EmbeddableType"

        val objectType = GraphQLObjectType.newObject()
                .name(embeddableTypeName)
                //.description(getSchemaDescription(embeddableType.javaType))
                .fields(embeddableType.attributes.stream()
                        //.filter { this.isNotIgnored(it) }
                        //.map<GraphQLFieldDefinition>(Function<Attribute<in *, *>, GraphQLFieldDefinition> { this.getObjectField(it) })
                        .map<GraphQLFieldDefinition>{getObjectField(it) }.toList()
                        //.collect<List<GraphQLFieldDefinition>, Any>(Collectors.toList())
                )
                .build()

        (embeddableCache as java.util.Map<EmbeddableType<*>, GraphQLObjectType>).putIfAbsent(embeddableType, objectType)

        return objectType
    }


    private val embeddableInputCache = HashMap<EmbeddableType<*>, GraphQLInputObjectType>()
    private fun getEmbeddableInputType(embeddableType: EmbeddableType<*>): GraphQLInputObjectType {
        if (embeddableInputCache.containsKey(embeddableType))
            return embeddableInputCache.get(embeddableType)!!

        val embeddableTypeName = namingStrategy.singularize(embeddableType.javaType.simpleName) + "EmbeddableType"

        val objectType = GraphQLInputObjectType.newInputObject()
                .name(embeddableTypeName)
                //.description(getSchemaDescription(embeddableType.javaType))
                .fields(embeddableType.attributes.stream()
                        //.filter { this.isNotIgnored(it) }
                        //.map<GraphQLFieldDefinition>(Function<Attribute<in *, *>, GraphQLFieldDefinition> { this.getObjectField(it) })
                        .map{getInputObjectField(it) }.toList()
                        //.collect<List<GraphQLFieldDefinition>, Any>(Collectors.toList())
                )
                .build()

        embeddableInputCache.putIfAbsent(embeddableType, objectType)

        return objectType
    }


    fun isEmbeddable(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.EMBEDDED
    }

    fun isBasic(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.BASIC
    }

    fun isElementCollection(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.ELEMENT_COLLECTION
    }

    fun isToMany(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_MANY || attribute.persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_MANY
    }

    fun isToOne(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_ONE || attribute.persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_ONE
    }

    /*
    fun isValidInput(attribute: Attribute<*, *>): Boolean {
        return attribute.persistentAttributeType == Attribute.PersistentAttributeType.BASIC || attribute.persistentAttributeType == Attribute.PersistentAttributeType.ELEMENT_COLLECTION
    }


    private fun isIdentity(attribute: Attribute<*, *>): Boolean {
        return attribute is SingularAttribute<*, *> && attribute.isId
    }
    */

    private val classCache = HashMap<Class<*>, GraphQLType>()
    private fun getGraphQLTypeFromJavaType(clazz: Class<*>): GraphQLType {
        if (clazz.isEnum) {

            if (classCache.containsKey(clazz)) {
                return (classCache.get(clazz)!!)
            }

            val enumBuilder = GraphQLEnumType.newEnum().name(clazz.simpleName)
            var ordinal = 0
            for (enumValue in (clazz as Class<Enum<*>>).enumConstants)
                enumBuilder.value(enumValue.name, ordinal++)

            val enumType = enumBuilder.build()
            setNoOpCoercing(enumType)

            (classCache as java.util.Map<Class<*>, GraphQLType>).putIfAbsent(clazz, enumType)

            return enumType
        }

        return JavaScalars.of(clazz)  //GraphQLScalarType is a GraphQLInputType 
    }



    private val inputClassCache = HashMap<Class<*>, GraphQLInputType>()
    private fun getGraphQLInputTypeFromJavaType(clazz: Class<*>): GraphQLInputType {
        if (clazz.isEnum) {

            if (inputClassCache.containsKey(clazz)) {
                return (inputClassCache.get(clazz)!!)
            }

            val enumBuilder = GraphQLEnumType.newEnum().name(clazz.simpleName)
            var ordinal = 0
            for (enumValue in (clazz as Class<Enum<*>>).enumConstants)
                enumBuilder.value(enumValue.name, ordinal++)

            val enumType = enumBuilder.build()
            setNoOpCoercing(enumType)

            inputClassCache.putIfAbsent(clazz, enumType)

            return enumType
        }

        return JavaScalars.of(clazz)  //GraphQLScalarType is a GraphQLInputType 
    }



    /**
     * JPA will deserialize Enum's for us...we don't want GraphQL doing it.
     *
     * @param type
     */
    private fun setNoOpCoercing(type: GraphQLType) {
        try {
            val coercing = type.javaClass.getDeclaredField("coercing")
            coercing.isAccessible = true
            coercing.set(type, NoOpCoercing())
        } catch (e: Exception) {
            //log.error("Unable to set coercing for $type", e)
        }

    }

    internal class NoOpCoercing : Coercing<Any, Any> {

        override fun serialize(input: Any): Any {
            return input
        }

        override fun parseValue(input: Any): Any {
            return input
        }

        override fun parseLiteral(input: Any): Any {
            return input
        }

    }

}